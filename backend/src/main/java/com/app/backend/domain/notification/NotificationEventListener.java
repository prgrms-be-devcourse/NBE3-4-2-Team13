package com.app.backend.domain.notification;

import com.app.backend.domain.notification.dto.NotificationEvent;
import com.app.backend.domain.notification.dto.NotificationMessage;
import com.app.backend.domain.notification.dto.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final NotificationProducer notificationProducer;

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        NotificationMessage message = new NotificationMessage(
                event.getId(),
                event.getUserId(),
                event.getTitle(),
                event.getContent(),
                LocalDateTime.now(),
                false
        );
        notificationProducer.sendNotification(message);
    }
}
