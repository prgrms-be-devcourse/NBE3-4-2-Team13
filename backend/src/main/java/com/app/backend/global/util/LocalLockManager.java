package com.app.backend.global.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class LocalLockManager {

    private static final ConcurrentMap<String, ReentrantLock> localLockMap = new ConcurrentHashMap<>();

    public ReentrantLock getLock(final String key) {
        return localLockMap.computeIfAbsent(key, k -> new ReentrantLock());
    }

    public void releaseLock(final String key, final ReentrantLock lock) {
        if (!lock.hasQueuedThreads())
            localLockMap.remove(key);
    }

}
