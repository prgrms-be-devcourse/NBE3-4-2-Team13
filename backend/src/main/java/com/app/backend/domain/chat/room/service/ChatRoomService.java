package com.app.backend.domain.chat.room.service;

import org.springframework.stereotype.Service;

import com.app.backend.domain.chat.room.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
}
