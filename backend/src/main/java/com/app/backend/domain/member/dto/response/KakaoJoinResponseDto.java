package com.app.backend.domain.member.dto.response;

import com.app.backend.domain.member.entity.Member;

import java.time.LocalDateTime;

public record KakaoJoinResponseDto(
    Long id,
    String username,
    String nickname,
    String role,
    LocalDateTime createdAt
) {
    public static KakaoJoinResponseDto from(Member member) {
        return new KakaoJoinResponseDto(
            member.getId(),
            member.getUsername(),
            member.getNickname(),
            member.getRole(),
            member.getCreatedAt()
        );
    }
} 