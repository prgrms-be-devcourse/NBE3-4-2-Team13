package com.app.backend.domain.group.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum GroupErrorCode implements DomainErrorCode {

    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GR001", "모임을 찾지 못함"),
    GROUP_NOT_IN_RECRUITMENT_STATUS(HttpStatus.NOT_FOUND, "GR002", "모임의 모집 상태가 닫혀 있음"),
    GROUP_MAXIMUM_NUMBER_OF_MEMBERS(HttpStatus.BAD_REQUEST, "GR003", "모임이 최대 모집 한도에 도달함");

    private final HttpStatus status;
    private final String     code;
    private final String     message;

}
