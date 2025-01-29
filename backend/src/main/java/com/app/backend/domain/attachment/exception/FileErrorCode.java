package com.app.backend.domain.attachment.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements DomainErrorCode {

    FILE_NOT_FOUND(HttpStatus.NOT_FOUND,"F001", "파일 정보가 존재하지 않음"),
    FILE_UNAUTHORIZATION(HttpStatus.FORBIDDEN, "F002", "파일 접근 권한이 없음"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST,"F003", "지원하지 않는 파일형식"),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST,"F004", "파일 확장자를 알수없음"),
    FAILED_FILE_SAVE(HttpStatus.BAD_REQUEST, "F005", "파일 저장 실패"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F006", "파일 용량 초과");

    private final HttpStatus status;
    private final String code;
    private final String message;

}