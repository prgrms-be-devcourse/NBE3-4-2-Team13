package com.app.backend.global.util;

import jakarta.validation.constraints.Min;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LockHelper {

    private static RedissonClient redissonClient;
    private static LockUtil       lockUtil;

    public static <R> R executeWithLock(final String lockKey,
                                        @Min(0) long maxWaitTime,
                                        @Min(0) long leaseTime,
                                        final TimeUnit timeUnit,
                                        final Callable<R> block) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lockUtil.lockWithRetry(lock, timeUnit.toMillis(maxWaitTime), timeUnit.toMillis(leaseTime)))
            try {
                return block.call();
            } catch (Exception e) {
                throw new RuntimeException("Failed to execute block", e);
            } finally {
                if (lock.isHeldByCurrentThread())
                    lock.unlock();
            }
        else
            throw new RuntimeException("Failed to acquire lock: " + lockKey);
    }

    public static void executeWithLock(final String lockKey,
                                       final long maxWaitTime,
                                       final long leaseTime,
                                       final TimeUnit timeUnit,
                                       final Runnable block) {
        executeWithLock(lockKey, maxWaitTime, leaseTime, timeUnit, () -> {
            block.run();
            return null;
        });
    }

    public static <R> R executeWithLock(final String lockKey,
                                        final long maxWaitTime,
                                        final long leaseTime,
                                        final Callable<R> block) {
        return executeWithLock(lockKey, maxWaitTime, leaseTime, TimeUnit.MILLISECONDS, block);
    }

    public static void executeWithLock(final String lockKey,
                                       final long maxWaitTime,
                                       final long leaseTime,
                                       final Runnable block) {
        executeWithLock(lockKey, maxWaitTime, leaseTime, TimeUnit.MILLISECONDS, block);
    }

    public static <R> R executeWithLock(final String lockKey, final Callable<R> block) {
        return executeWithLock(lockKey, 1000L, 5000L, TimeUnit.MILLISECONDS, block);
    }

    public static void executeWithLock(final String lockKey, final Runnable block) {
        executeWithLock(lockKey, 1000L, 5000L, TimeUnit.MILLISECONDS, block);
    }

    @Autowired
    public void setRedissonClient(final RedissonClient _redissonClient) {
        LockHelper.redissonClient = _redissonClient;
    }

    @Autowired
    public void setLockUtil(final LockUtil _lockUtil) {
        LockHelper.lockUtil = _lockUtil;
    }

}
