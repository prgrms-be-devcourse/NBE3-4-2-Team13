package com.app.backend.domain.notification.dto;

import com.app.backend.domain.notification.SseEmitters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final SseEmitters sseEmitters;

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consume(NotificationMessage message) {
        try {
            log.info("Kafka message received: {}", message);
            // SSE를 통해 클라이언트에게 알림 전송
            sseEmitters.sendToUser(message.getUserId(), message);
            log.info("Notification sent via SSE: {}", message);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
        }
    }
}