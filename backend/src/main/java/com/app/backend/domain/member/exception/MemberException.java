package com.app.backend.domain.member.exception;

public class MemberException extends RuntimeException{
	private MemberErrorCode errorCode;
	private String message;

}
