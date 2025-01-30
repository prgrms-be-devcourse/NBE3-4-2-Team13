package com.app.backend.domain.member.dto.kakao;

public record TokenDto(
    String accessToken,
    String refreshToken
) {} 