package com.app.backend.domain.notification.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {
    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;
    private static final String TOPIC = "notification-topic";

    public void sendNotification(NotificationMessage message) {
        kafkaTemplate.send(TOPIC, message.getUserId(), message);
    }
}