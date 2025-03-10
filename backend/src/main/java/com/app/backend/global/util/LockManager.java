package com.app.backend.global.util;

import com.app.backend.global.util.LockUtil.LockWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockManager {

    private final static int  MAX_UNLOCK_RETRY_COUNT = 3;
    private final static long RETRY_DELAY            = 100L;

    private final RedissonClient redissonClient;
    private final LockUtil       lockUtil;

    public LockWrapper acquireLock(final String lockKey, final long maxWaitTime, final long leaseTime) {
        if (!isRedisRunning())
            throw new IllegalStateException("Redis server is not available");
        RLock lock = redissonClient.getLock(lockKey);
        return LockWrapper.of(lockKey, lock, lockUtil.lockWithRetry(lock, maxWaitTime, leaseTime));
    }

    public void releaseLock(final LockUtil.LockWrapper lockWrapper) {
        if (lockWrapper.isLocked())
            lockUtil.unlockWithRetry(lockWrapper.getLock(), 0);
    }

    public void registerLockReleaseAfterTransaction(final LockUtil.LockWrapper lockWrapper) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                releaseLock(lockWrapper);
            }
        });
    }

    private boolean isRedisRunning() {
        try {
            redissonClient.getKeys().count();
            return true;
        } catch (RedisConnectionException e) {
            log.warn("Redis server is not available. Switching to local lock", e);
            return false;
        }
    }

}
