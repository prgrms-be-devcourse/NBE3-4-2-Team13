package com.app.backend.domain.category.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;

public class CategoryException extends DomainException {
	public CategoryException(final DomainErrorCode errorCode) {
		super(errorCode);
	}
}