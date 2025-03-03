package com.app.backend.global.config;

import com.app.backend.global.aop.AppAspect.LockAspect;
import com.app.backend.global.aop.AppAspect.PageJsonSerializerAspect;
import com.app.backend.global.util.LockManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {

    @Bean
    public PageJsonSerializerAspect pageJsonSerializerAspect() {
        return new PageJsonSerializerAspect();
    }

    @Bean
    public LockAspect redissonLockAspect(final LockManager lockManager) {
        return new LockAspect(lockManager);
    }

}
