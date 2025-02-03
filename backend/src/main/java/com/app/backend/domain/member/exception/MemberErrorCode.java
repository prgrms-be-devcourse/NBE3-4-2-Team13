package com.app.backend.domain.member.exception;

import org.springframework.http.HttpStatus;

import com.app.backend.global.error.exception.DomainErrorCode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MemberErrorCode implements DomainErrorCode {
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MB001", "회원을 찾지 못함"),
	MEMBER_USERNAME_EXISTS(HttpStatus.BAD_REQUEST, "MB002", "이미 존재하는 아이디"),
	MEMBER_NICKNAME_EXISTS(HttpStatus.BAD_REQUEST, "MB003", "이미 존재하는 닉네임"),
	MEMBER_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "MB004", "비밀번호가 일치하지 않음"),
	MEMBER_IS_DISABLED(HttpStatus.BAD_REQUEST, "MB005", "비활성화된 회원"),
	MEMBER_UNVALID_TOKEN(HttpStatus.BAD_REQUEST, "MB005", "유효하지 않은 토큰"),
	MEMBER_FAILED_TO_MODIFY(HttpStatus.BAD_REQUEST, "MB006", "회원 정보 수정 실패"),
	MEMBER_NO_ADMIN_PERMISSION(HttpStatus.FORBIDDEN, "MB007", "관리자 권한이 없음"),
	MEMBER_FAILED_TO_KAKAO_TOKEN(HttpStatus.BAD_REQUEST, "MB008", "카카오 토큰 발급 실패"),
	MEMBER_FAILED_TO_KAKAO_AUTH(HttpStatus.BAD_REQUEST, "MB009", "카카오 인증 실패");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
