package com.app.backend.global.aop;

import com.app.backend.global.annotation.CustomLock;
import com.app.backend.global.annotation.CustomPageJsonSerializer;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.module.CustomPageModule;
import com.app.backend.global.util.LocalLockManager;
import com.app.backend.global.util.LockKeyGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
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
    public static class LockAspect {

        private final static int  MAX_UNLOCK_RETRY_COUNT = 3;
        private final static long RETRY_DELAY            = 100L;

        private final ExecutorService          executorService = Executors.newFixedThreadPool(10);
        private final ScheduledExecutorService scheduler       = Executors.newScheduledThreadPool(1);

        private final Optional<RedissonClient> redissonClient;
        private final LocalLockManager         localLockManager;

        @Around("@annotation(customLock)")
        public Object execute(ProceedingJoinPoint joinPoint, CustomLock customLock) throws Throwable {
            String      lockKey     = LockKeyGenerator.generateLockKey(joinPoint, customLock.key());
            LockWrapper lockWrapper = acquireLock(lockKey, customLock.maxWaitTime(), customLock.leaseTime());

            if (!lockWrapper.isLocked())
                throw new RuntimeException("Failed to acquire lock: " + lockKey);

            try {
                Object result = joinPoint.proceed();

                if (TransactionSynchronizationManager.isActualTransactionActive())
                    registerLockReleaseAfterTransaction(lockWrapper);
                else
                    releaseLock(lockWrapper);

                return result;
            } catch (Throwable e) {
                releaseLock(lockWrapper);
                throw e;
            }
        }

        private LockWrapper acquireLock(final String lockKey, final long maxWaitTime, final long leaseTime) {
            boolean isRedisAvailable = isRedisRunning();

            RLock         redisLock = isRedisAvailable ? redissonClient.get().getLock(lockKey) : null;
            ReentrantLock localLock = isRedisAvailable ? null : localLockManager.getLock(lockKey);

            boolean locked = isRedisAvailable
                             ? tryRedissonLock(redisLock, maxWaitTime, leaseTime)
                             : tryLocalLock(localLock, maxWaitTime);

            return LockWrapper.of(lockKey, redisLock, localLock, isRedisAvailable && locked, locked);
        }

        private boolean isRedisRunning() {
            return redissonClient.map(client -> {
                try {
                    client.getKeys().count();
                    return true;
                } catch (RedisConnectionException e) {
                    log.warn("Redis server is not available. Switching to local lock", e);
                    return false;
                }
            }).orElse(false);
        }

        private boolean tryRedissonLock(final RLock lock, final long maxWaitTime, final long leaseTime) {
            try {
                return CompletableFuture.supplyAsync(() -> {
                    long baseDelay   = 100L;
                    long elapsedTime = 0L;

                    while (elapsedTime < maxWaitTime) {
                        try {
                            if (lock.tryLock(0, leaseTime, TimeUnit.MILLISECONDS))
                                return true;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Redis lock acquisition interrupted", e);
                        }

                        log.info("Redis lock acquisition failed, retrying after wait time: {}ms", baseDelay);
                        sleep(baseDelay);

                        elapsedTime += baseDelay;
                        baseDelay = Math.min(baseDelay * 2, maxWaitTime - elapsedTime);
                    }
                    return false;
                }, executorService).get();
            } catch (InterruptedException e) {
                log.error("Redisson lock acquisition interrupted", e);
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                log.error("Execution exception occurred", e);
                return false;
            }
        }

        private boolean tryLocalLock(final ReentrantLock lock, final long maxWaitTime) {
            try {
                return CompletableFuture.supplyAsync(() -> {
                    long baseDelay   = 100L;
                    long elapsedTime = 0L;

                    while (elapsedTime < maxWaitTime) {
                        try {
                            if (lock.tryLock(0, TimeUnit.MILLISECONDS))
                                return true;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Local lock acquisition interrupted", e);
                        }

                        log.info("Local lock acquisition failed, retrying after wait time: {}ms", baseDelay);
                        sleep(baseDelay);

                        elapsedTime += baseDelay;
                        baseDelay = Math.min(baseDelay * 2, maxWaitTime - elapsedTime);
                    }
                    return false;
                }, executorService).get();
            } catch (InterruptedException e) {
                log.error("Local lock acquisition interrupted", e);
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                log.error("Execution exception occurred", e);
                return false;
            }
        }

        private void releaseLock(final LockWrapper lockWrapper) {
            if (lockWrapper.usingRedisLock)
                unlockRedissonLock(lockWrapper.redisLock, 0);
            else {
                unlockLocalLock(lockWrapper.localLock);
                localLockManager.releaseLock(lockWrapper.lockKey, lockWrapper.localLock);
            }
        }

        private void unlockRedissonLock(final RLock lock, int retryCount) {
            if (lock.isLocked() && lock.isHeldByCurrentThread())
                lock.unlockAsync().whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        log.warn("Failed to unlock redisson lock, retrying {}/{}", retryCount + 1,
                                 MAX_UNLOCK_RETRY_COUNT);
                        if (retryCount < MAX_UNLOCK_RETRY_COUNT)
                            scheduler.schedule(
                                    () -> unlockRedissonLock(lock, retryCount + 1), RETRY_DELAY, TimeUnit.MILLISECONDS
                            );
                        else
                            forceUnlockRedissonLock(lock);
                    } else
                        log.info("Redisson lock successfully unlocked");
                });
        }

        private void forceUnlockRedissonLock(final RLock lock) {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.forceUnlock();
                log.warn("Redisson lock forcefully unlocked after max retries");
            } else
                log.warn("Skipping force unlock, lock is not held by current thread");
        }

        private void unlockLocalLock(final ReentrantLock lock) {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }

        private void registerLockReleaseAfterTransaction(final LockWrapper lockWrapper) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    releaseLock(lockWrapper);
                }
            });
        }

        private void sleep(final long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread sleep interrupted", e);
            }
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
        }

    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class LockWrapper {
        private final String        lockKey;
        private final RLock         redisLock;
        private final ReentrantLock localLock;
        private final boolean       usingRedisLock;
        private final boolean       locked;

        private static LockWrapper of(final String lockKey,
                                      final RLock redisLock,
                                      final ReentrantLock localLock,
                                      final boolean usingRedisLock,
                                      final boolean locked) {
            return LockWrapper.builder()
                              .lockKey(lockKey)
                              .redisLock(redisLock)
                              .localLock(localLock)
                              .usingRedisLock(usingRedisLock)
                              .locked(locked)
                              .build();
        }
    }

}
