package com.app.backend.domain.member.dto.request;

public record MemberLoginRequestDto(
    String username,
    String password
) {
}
