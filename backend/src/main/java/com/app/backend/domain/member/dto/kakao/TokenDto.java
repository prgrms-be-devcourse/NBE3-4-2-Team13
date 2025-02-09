package com.app.backend.domain.member.dto.kakao;

public record TokenDto(
	String id,
	String accessToken,
	String refreshToken,
	String role
) {} 