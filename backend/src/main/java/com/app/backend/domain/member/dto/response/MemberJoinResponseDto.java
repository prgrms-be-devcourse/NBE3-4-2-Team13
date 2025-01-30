package com.app.backend.domain.member.dto.response;

import com.app.backend.domain.member.entity.Member;

import java.time.LocalDateTime;

public record MemberJoinResponseDto(
    Long id,
    String username,
    String nickname,
    String role,
    LocalDateTime createdAt
) {
    // Entity -> DTO 변환을 위한 정적 팩토리 메서드
    public static MemberJoinResponseDto from(Member member) {
        return new MemberJoinResponseDto(
            member.getId(),
            member.getUsername(),
            member.getNickname(),
            member.getRole(),
            member.getCreatedAt()
        );
    }
} 