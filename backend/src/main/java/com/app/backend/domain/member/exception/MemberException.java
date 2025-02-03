package com.app.backend.domain.member.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;

public class MemberException extends DomainException {
	public MemberException(DomainErrorCode domainErrorCode) {
		super(domainErrorCode);
	}
}
