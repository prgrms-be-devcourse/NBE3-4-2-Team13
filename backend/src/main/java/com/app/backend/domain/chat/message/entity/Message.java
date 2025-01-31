package com.app.backend.domain.chat.message.entity;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "messages")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Message {

	@Id
	@Field("chat_message_id")
	private ObjectId id;

	@Field("chat_room_id")
	private Long chatRoomId;

	@Field("member_id")
	private Long senderId;

	@Field("member_nickname")
	private String senderNickname;

	@Field("content")
	private String content;

	@Field("disabled")
	private Boolean disabled = false;

	@CreatedDate
	@Field("createdAt")
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Field("modifiedAt")
	private LocalDateTime modifiedAt;


	/**
	 * 메세지 활성화 (디폴트 값)
	 */
	public void activate() {
		disabled = false;
	}

	/**
	 * 메세지 삭제
	 */
	public void deactivate() {
		disabled = true;
	}
}
