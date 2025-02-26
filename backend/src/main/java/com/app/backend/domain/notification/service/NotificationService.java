package com.app.backend.domain.notification.service;

import com.app.backend.domain.notification.dto.NotificationEvent;
import com.app.backend.domain.notification.dto.NotificationMessage;
import com.app.backend.domain.notification.dto.NotificationProducer;
import com.app.backend.domain.notification.entity.Notification;
import com.app.backend.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationProducer notificationProducer;
    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;

    public void sendNotification(String userId, String title, String content,
                                 NotificationEvent.NotificationType type, Long targetId) {
        // 알림 저장
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .isRead(false)
                .type(type)
                .targetId(targetId)
                .createdAt(LocalDateTime.now())
                .build();
        notification = notificationRepository.save(notification);
        // Kafka로 메시지 전송
        NotificationMessage message = new NotificationMessage(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getCreatedAt(),
                notification.isRead()
        );
        kafkaTemplate.send(NotificationEvent.NotificationType.GROUP_INVITE.toString(), userId, message);
        notificationProducer.sendNotification(message);
    }

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationMessage> getNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> new NotificationMessage(
                        notification.getId(),
                        notification.getUserId(),
                        notification.getTitle(),
                        notification.getContent(),
                        notification.getCreatedAt(),
                        notification.isRead()
                ))
                .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다."));
        notification.markAsRead();
        notificationRepository.save(notification);
    }
}