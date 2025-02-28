package com.app.backend.global.aop;

import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.global.annotation.CustomCache;
import com.app.backend.global.annotation.CustomCacheDelete;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private static final String UPDATE_KEY = "update";
    private static final String HISTORY_KEY = "history";
    private static final String VIEW_COUNT_PREFIX = "viewCount";
    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(customCache)")
    public Object aroundG(ProceedingJoinPoint joinPoint, CustomCache customCache) throws Throwable {
        String cacheKey = generateKey(customCache.prefix(), customCache.key(), customCache.id(), getParams(joinPoint));
        String viewCountKey = VIEW_COUNT_PREFIX + ":" + cacheKey;
        String limitUserKey = cacheKey + ":user:" + getUserID();
        String updateKeyList = customCache.prefix() + ":" + UPDATE_KEY;
        String historyKey = customCache.prefix() + ":" + HISTORY_KEY;

        Object result = null;

        try {
            // 조회수
            if (customCache.viewCount() && !redisTemplate.hasKey(limitUserKey)) {
                redisTemplate.opsForValue().increment(viewCountKey);
                redisTemplate.opsForValue().set(limitUserKey, true, customCache.viewCountTtl(), customCache.viewCountTtlUnit());

                redisTemplate.opsForSet().add(updateKeyList, viewCountKey);
            }

            Object cachedData = redisTemplate.opsForValue().get(cacheKey);

            // 조회 기록
            if (customCache.history()) {
                redisTemplate.opsForSet().add(historyKey, cacheKey);
            }

            if (cachedData != null) {
                return cachedData;
            }

            result = joinPoint.proceed();

            redisTemplate.opsForValue().set(cacheKey, result, customCache.ttl(), customCache.ttlUnit());

            return result;
        } catch (Exception e) {
            return joinPoint.proceed();
        }
    }

    @Around("@annotation(customCacheDelete)")
    public Object aroundD(ProceedingJoinPoint joinPoint, CustomCacheDelete customCacheDelete) throws Throwable {
        String cacheKey = generateKey(customCacheDelete.prefix(), customCacheDelete.key(), customCacheDelete.id(), getParams(joinPoint));

        try {

            if (redisTemplate.hasKey(cacheKey)) {
                redisTemplate.delete(cacheKey);
            }

            return joinPoint.proceed();
        } catch (Exception e) {
            return joinPoint.proceed();
        }
    }

    private Long getUserID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
        return memberDetails.getId();
    }

    private Map<String, Object> getParams(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] paramsName = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> params = new HashMap<>();

        for (int i = 0; i < paramsName.length; i++) {
            params.put(paramsName[i], args[i]);
        }

        return params;
    }

    private String generateKey(String prefix, String key, String id, Map<String, Object> params) {
        StringBuilder newKey = new StringBuilder(prefix);

        if (!key.isEmpty()) {
            newKey.append(":").append(key);
        }

        if (!id.isEmpty() && params.containsKey(id)) {
            newKey.append(":").append(params.get(id));
            return newKey.toString();
        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            newKey.append(":").append(entry.getValue().toString());
        }

        return newKey.toString();
    }
}
