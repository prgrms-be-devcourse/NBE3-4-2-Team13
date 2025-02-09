package com.app.backend.domain.member.dto.response;

import com.app.backend.domain.member.entity.Member;

public record MemberLoginResponseDto(
    Long id,
    String username,
    String nickname,
    String role,
    String createdAt,
    String modifiedAt,
    String accessToken,
    String refreshToken
) {
    public static MemberLoginResponseDto of(Member member, String accessToken, String refreshToken) {
        return new MemberLoginResponseDto(
            member.getId(),
            member.getUsername(),
            member.getNickname(),
            member.getRole(),
            String.valueOf(member.getCreatedAt()),
            String.valueOf(member.getModifiedAt()),
            accessToken,
            refreshToken
        );
    }
} 