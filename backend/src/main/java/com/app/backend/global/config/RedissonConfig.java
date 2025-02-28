package com.app.backend.global.config;

import com.app.backend.global.config.condition.RedisAvailableCondition;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${redisson.host:localhost}")
    private String host;

    @Value("${redisson.port:6380}")
    private int port;

    @Value("${redisson.password}")
    private String password;

    @Bean
    @Conditional(RedisAvailableCondition.class)
    public RedissonClient redissonClient() {
        Config config = new Config();
        if (password != null && !password.isBlank())
            config.useSingleServer()
                  .setAddress("redis://%s:%d".formatted(host, port))
                  .setPassword(password)
                  .setConnectionPoolSize(64);
        else
            config.useSingleServer()
                  .setAddress("redis://%s:%d".formatted(host, port))
                  .setConnectionPoolSize(64);
        return Redisson.create(config);
    }

}
