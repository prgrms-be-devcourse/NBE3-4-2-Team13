package com.app.backend.global.aop;

import com.app.backend.global.annotation.CustomLock;
import com.app.backend.global.annotation.CustomPageJsonSerializer;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.util.LockKeyGenerator;
import com.app.backend.global.util.LockManager;
import com.app.backend.global.util.LockUtil.LockWrapper;
import com.app.backend.global.util.PageUtil;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class AppAspect {

    @Aspect
    public static class PageJsonSerializerAspect {

        @Around("@annotation(com.app.backend.global.annotation.CustomPageJsonSerializer)")
        public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method          method    = signature.getMethod();
            if (method == null)
                return joinPoint.proceed();

            CustomPageJsonSerializer annotation = method.getAnnotation(CustomPageJsonSerializer.class);
            if (annotation == null)
                return joinPoint.proceed();

            Object result = joinPoint.proceed();

            if (result instanceof Page<?> page)
                return PageUtil.processPageJson(page, annotation);
            else if (result instanceof ApiResponse<?> apiResponse)
                return PageUtil.processApiResponse(apiResponse, annotation);
            else if (result instanceof ResponseEntity<?> responseEntity)
                return PageUtil.processResponseEntity(responseEntity, annotation);
            else
                return result;
        }

    }

    @Aspect
    @RequiredArgsConstructor
    public static class LockAspect {

        private final LockManager lockManager;

        @Around("@annotation(customLock)")
        public Object execute(ProceedingJoinPoint joinPoint, CustomLock customLock) throws Throwable {
            String lockKey = LockKeyGenerator.generateLockKey(joinPoint, customLock.key());
            LockWrapper lockWrapper = lockManager.acquireLock(lockKey,
                                                              customLock.timeUnit().toMillis(customLock.maxWaitTime()),
                                                              customLock.timeUnit().toMillis(customLock.leaseTime()));

            if (!lockWrapper.isLocked())
                throw new RuntimeException("Failed to acquire lock: " + lockKey);

            try {
                Object result = joinPoint.proceed();

                if (TransactionSynchronizationManager.isActualTransactionActive())
                    lockManager.registerLockReleaseAfterTransaction(lockWrapper);
                else
                    lockManager.releaseLock(lockWrapper);

                return result;
            } catch (Throwable e) {
                lockManager.releaseLock(lockWrapper);
                throw e;
            }
        }
    }

}
