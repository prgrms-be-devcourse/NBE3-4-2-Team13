package com.app.backend.global.config;

import com.app.backend.global.aop.AppAspect.PageJsonSerializerAspect;
import com.app.backend.global.aop.AppAspect.RedissonLockAspect;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {

    @Bean
    public PageJsonSerializerAspect pageJsonSerializerAspect() {
        return new PageJsonSerializerAspect();
    }

    @Bean
    public RedissonLockAspect redissonLockAspect(final RedissonClient redissonClient) {
        return new RedissonLockAspect(redissonClient);
    }

}
