package com.app.backend.domain.chat.message.dto.request;

public record MessageRequest (Long chatRoomId, Long senderId, String senderNickname, String content){
}
