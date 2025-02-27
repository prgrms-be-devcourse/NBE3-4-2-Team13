package com.app.backend.global.aop;

import com.app.backend.global.annotation.CustomLock;
import com.app.backend.global.annotation.CustomPageJsonSerializer;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.module.CustomPageModule;
import com.app.backend.global.util.LockKeyGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class AppAspect {

    @Aspect
    public static class PageJsonSerializerAspect {

        private static final ConcurrentMap<String, ObjectMapper> objectMapperMap = new ConcurrentHashMap<>();

        @Around("@annotation(com.app.backend.global.annotation.CustomPageJsonSerializer)")
        public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method          method    = signature.getMethod();

            CustomPageJsonSerializer annotation = method.getAnnotation(CustomPageJsonSerializer.class);
            if (annotation == null) return joinPoint.proceed();

            Object result = joinPoint.proceed();

            if (result instanceof ApiResponse<?> apiResponse) {
                Object body = apiResponse.getData();

                if (body instanceof Page<?>) {
                    String key = generateKey(annotation);
                    ObjectMapper objectMapper = objectMapperMap.computeIfAbsent(key, newKey -> {
                        log.info("새로운 ObjectMapper 생성: {}", newKey);
                        ObjectMapper newObjectMapper = new ObjectMapper();
                        newObjectMapper.registerModules(new JavaTimeModule(), new CustomPageModule(annotation));
                        newObjectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                        return newObjectMapper;
                    });

                    String   json     = objectMapper.writeValueAsString(body);
                    JsonNode jsonNode = objectMapper.readTree(json);

                    return ApiResponse.of(apiResponse.getIsSuccess(), apiResponse.getCode(), apiResponse.getMessage(),
                                          jsonNode);
                }
            }

            return result;
        }

        private String generateKey(CustomPageJsonSerializer annotation) {
            return annotation.content() + "_" + annotation.hasContent() + "_" + annotation.totalPages() + "_" +
                   annotation.totalElements() + "_" + annotation.numberOfElements() + "_" + annotation.size() + "_" +
                   annotation.number() + "_" + annotation.hasPrevious() + "_" + annotation.hasNext() + "_" +
                   annotation.isFirst() + "_" + annotation.isLast() + "_" + annotation.sort() + "_" +
                   annotation.empty();
        }

    }

    @Aspect
    @RequiredArgsConstructor
    public static class RedissonLockAspect {

        private final static int  MAX_UNLOCK_RETRY_COUNT = 3;
        private final static long RETRY_DELAY            = 100;

        private final ExecutorService          executorService = Executors.newFixedThreadPool(10);
        private final ScheduledExecutorService scheduler       = Executors.newScheduledThreadPool(1);

        private final RedissonClient redissonClient;

        @Around("@annotation(com.app.backend.global.annotation.CustomLock)")
        public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
            MethodSignature signature  = (MethodSignature) joinPoint.getSignature();
            Method          method     = signature.getMethod();
            CustomLock      annotation = method.getAnnotation(CustomLock.class);

            if (annotation == null) return joinPoint.proceed();

            String lockKey = LockKeyGenerator.generateLockKey(joinPoint, annotation.key());
            log.info("LockKey: {}", lockKey);

            RLock lock        = redissonClient.getLock(lockKey);
            long  maxWaitTime = annotation.maxWaitTime();
            long  leaseTime   = annotation.leaseTime();

            try {
                boolean lockAcquired = CompletableFuture.supplyAsync(() -> {
                    long baseDelay   = 100L;
                    long elapsedTime = 0L;

                    while (elapsedTime < maxWaitTime) {
                        try {
                            if (lock.tryLock(0, leaseTime, TimeUnit.MILLISECONDS)) return true;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Lock acquisition interrupted", e);
                        }

                        log.info("Lock acquisition failed, retrying after wait time: {}ms", baseDelay);
                        try {
                            Thread.sleep(baseDelay);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Retry sleep interrupted", e);
                        }

                        elapsedTime += baseDelay;
                        baseDelay = Math.min(baseDelay * 2, maxWaitTime - elapsedTime);
                    }
                    return false;
                }, executorService).get();

                if (!lockAcquired)
                    throw new RuntimeException("Lock acquisition failed, max wait time exceeded");

                try {
                    Object result = joinPoint.proceed();
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            retryUnlockAsync(lock, 0);
                        }
                    });
                    return result;
                } catch (Exception e) {
                    retryUnlockAsync(lock, 0);
                    throw e;
                } finally {
                    retryUnlockAsync(lock, 0);
                }
            } catch (ExecutionException e) {
                throw e.getCause() instanceof RuntimeException ? e.getCause() : new RuntimeException(e.getCause());
            }
        }

        private void retryUnlockAsync(final RLock lock, final int retryCount) {
            if (retryCount >= MAX_UNLOCK_RETRY_COUNT) {
                log.error("Lock release failed after {} attempts. Forcing unlock...", MAX_UNLOCK_RETRY_COUNT);
                lock.forceUnlock();
                return;
            }

            lock.unlockAsync().whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    log.warn("Lock release failed, retrying... (Attempt {}/{})",
                             retryCount + 1,
                             MAX_UNLOCK_RETRY_COUNT);
                    scheduler.schedule(
                            () -> retryUnlockAsync(lock, retryCount + 1), RETRY_DELAY, TimeUnit.MILLISECONDS
                    );
                } else
                    log.info("Lock successfully released after {} attempt(s)", retryCount + 1);
            });
        }

        @PreDestroy
        private void shutdownExecutors() {
            log.info("Shutting down executor services...");
            executorService.shutdown();
            scheduler.shutdown();

            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("ExecutorService did not terminate in the specified time.");
                    executorService.shutdownNow();
                }
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("ScheduledExecutorService did not terminate in the specified time.");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Shutdown interrupted", e);
                executorService.shutdownNow();
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("Executor services shut down successfully");
        }

    }

}
