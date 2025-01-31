package com.app.backend.domain.chat.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.backend.domain.chat.room.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {

}
