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
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
            if (annotation == null)
                return joinPoint.proceed();

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

                    return ApiResponse.of(apiResponse.getIsSuccess(),
                                          apiResponse.getCode(),
                                          apiResponse.getMessage(),
                                          jsonNode);
                }
            }

            return result;
        }

        private String generateKey(CustomPageJsonSerializer annotation) {
            return annotation.content() + "_" +
                   annotation.hasContent() + "_" +
                   annotation.totalPages() + "_" +
                   annotation.totalElements() + "_" +
                   annotation.numberOfElements() + "_" +
                   annotation.size() + "_" +
                   annotation.number() + "_" +
                   annotation.hasPrevious() + "_" +
                   annotation.hasNext() + "_" +
                   annotation.isFirst() + "_" +
                   annotation.isLast() + "_" +
                   annotation.sort() + "_" +
                   annotation.empty();
        }

    }

    @Aspect
    @RequiredArgsConstructor
    public static class RedissonLockAspect {

        private final RedissonClient redissonClient;

        @Around("@annotation(com.app.backend.global.annotation.CustomLock)")
        public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
            MethodSignature signature  = (MethodSignature) joinPoint.getSignature();
            Method          method     = signature.getMethod();
            CustomLock      annotation = method.getAnnotation(CustomLock.class);

            if (annotation == null)
                return joinPoint.proceed();

            String lockKey = LockKeyGenerator.generateLockKey(joinPoint, annotation.key());
            log.info("LockKey: {}", lockKey);

            RLock lock        = redissonClient.getLock(lockKey);
            long  maxWaitTime = annotation.maxWaitTime();
            long  leaseTime   = annotation.leaseTime();
            long  baseDelay   = 100L;
            long  elapsedTime = 0L;

            while (elapsedTime < maxWaitTime) {
                if (lock.tryLock(0, leaseTime, TimeUnit.MILLISECONDS))
                    try {
                        return joinPoint.proceed();
                    } finally {
                        lock.unlock();
                    }

                log.info("Lock acquisition failed, retrying after wait time: {}ms", baseDelay);
                Thread.sleep(baseDelay);
                elapsedTime += baseDelay;
                baseDelay = Math.min(baseDelay * 2, maxWaitTime - elapsedTime);
            }
            throw new RuntimeException("Lock acquisition failed, max wait time exceeded");
        }

    }

}
