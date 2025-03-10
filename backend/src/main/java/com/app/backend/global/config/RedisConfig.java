package com.app.backend.global.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {
    @Value("${spring.data.redis.host:localhost}")
    private String host;
    @Value("${spring.data.redis.port:6379}")
    private int    port;
    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(password);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                                                                    .allowIfBaseType(Object.class)
                                                                    .build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
        return template;
    }

    @Bean
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