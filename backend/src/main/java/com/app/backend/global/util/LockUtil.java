package com.app.backend.global.util;

import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockUtil {

    private final static int  MAX_UNLOCK_RETRY_COUNT = 3;
    private final static long RETRY_DELAY            = 100L;

    boolean lockWithRetry(final RLock lock, final long maxWaitTime, final long leaseTime) {
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
    }

    void unlockWithRetry(final RLock lock, int retryCount) {
        if (lock.isLocked() && lock.isHeldByCurrentThread())
            try {
                lock.unlock();
                log.info("Redisson lock successfully unlocked");
            } catch (Exception e) {
                log.warn("Failed to unlock redisson lock, retrying {}/{}", retryCount + 1,
                         MAX_UNLOCK_RETRY_COUNT);
                if (retryCount < MAX_UNLOCK_RETRY_COUNT) {
                    sleep(RETRY_DELAY);
                    unlockWithRetry(lock, retryCount + 1);
                } else
                    forceUnlock(lock);
            }
    }

    private void forceUnlock(final RLock lock) {
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.forceUnlock();
            log.warn("Redisson lock forcefully unlocked after max retries");
        } else
            log.warn("Skipping force unlock, lock is not held by current thread");
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread sleep interrupted", e);
        }
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class LockWrapper {
        private final String  lockKey;
        private final RLock   lock;
        private final boolean locked;

        public static LockWrapper of(final String lockKey,
                                     final RLock lock,
                                     final boolean locked) {
            return LockWrapper.builder()
                              .lockKey(lockKey)
                              .lock(lock)
                              .locked(locked)
                              .build();
        }
    }

}
