package com.app.backend.domain.member.dto.response;


import com.app.backend.domain.member.entity.Member;

public record KakaoLoginResponseDto(
        Long id,
        String nickname,
        String role,
        String accessToken,
        String refreshToken,
        String kakaoAccountId
) {
    public static KakaoLoginResponseDto of(Member member, String accessToken, String refreshToken) {
        return new KakaoLoginResponseDto(
                member.getId(),
                member.getNickname(),
                member.getRole(),
                accessToken,
                refreshToken,
                member.getOauthProviderId()
        );
    }
} 