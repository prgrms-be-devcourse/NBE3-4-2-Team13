package com.app.backend.domain.notification.controller;

import com.app.backend.domain.notification.SseEmitters;
import com.app.backend.domain.notification.entity.Notification;
import com.app.backend.domain.notification.service.NotificationService;
import com.app.backend.global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final SseEmitters sseEmitters;

    @GetMapping
    public ApiResponse<List<Notification>> getNotifications(@RequestParam String userId) {
        List<Notification> notifications = notificationService.getNotifications(userId);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "알림 목록 조회 성공",
                notifications
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "알림 읽음 처리 성공"
        );
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitters.add(userId, emitter);
        
        // 연결 종료 시 처리
        emitter.onCompletion(() -> sseEmitters.remove(userId));
        emitter.onTimeout(() -> sseEmitters.remove(userId));
        
        // 연결 즉시 테스트 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected!"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return emitter;
    }
}
