package com.app.backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class TestKafkaConfig {
    
    @Bean
    public NewTopic groupInviteTopic() {
        return TopicBuilder.name("GROUP_INVITE")
                          .partitions(1)
                          .replicas(1)
                          .build();
    }
    
    @Bean
    public KafkaAdmin.NewTopics topics() {
        return new KafkaAdmin.NewTopics(
            TopicBuilder.name("GROUP_INVITE")
                       .partitions(1)
                       .replicas(1)
                       .build()
        );
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name("notification-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
} 