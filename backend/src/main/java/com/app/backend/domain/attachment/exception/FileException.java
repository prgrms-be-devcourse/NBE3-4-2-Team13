package com.app.backend.domain.attachment.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;

public class FileException extends DomainException {

    public FileException(final DomainErrorCode errorCode) {super(errorCode);}

}
