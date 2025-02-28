package com.app.backend.global.config.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import redis.clients.jedis.Jedis;

public class RedisAvailableCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String host     = context.getEnvironment().getProperty("redisson.host", "localhost");
        int    port     = Integer.parseInt(context.getEnvironment().getProperty("redisson.port", "6380"));
        String password = context.getEnvironment().getProperty("redisson.password", "");

        try (Jedis jedis = new Jedis(host, port)) {
            if (!password.isEmpty())
                jedis.auth(password);
            return "PONG".equals(jedis.ping());
        } catch (Exception e) {
            return false;
        }
    }
}
