package com.app.backend.domain.group.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements DomainErrorCode {

    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GR001", "모임을 찾지 못함");

    private final HttpStatus status;
    private final String     code;
    private final String     message;

}
