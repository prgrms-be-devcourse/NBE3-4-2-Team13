package com.app.backend.domain.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SseEmitters {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void add(String userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
    }

    public void remove(String userId) {
        emitters.remove(userId);
    }

    public void sendToUser(String userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data));
            } catch (IOException e) {
                log.error("SSE 전송 실패: {}", e.getMessage());
                emitters.remove(userId);
            }
        }
    }
}