package com.app.backend.domain.member.dto.request;

public record KakaoLoginRequestDto(
        String username,
        String nickname,
        String oauthProviderId
) {
}
