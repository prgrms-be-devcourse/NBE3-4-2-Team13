package com.app.backend.domain.group.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum GroupMembershipErrorCode implements DomainErrorCode {

    GROUP_MEMBERSHIP_NOT_FOUND(HttpStatus.BAD_REQUEST, "GM001", "모임 멤버십을 찾을 수 없음"),
    GROUP_MEMBERSHIP_NO_PERMISSION(HttpStatus.FORBIDDEN, "GM002", "모임 수정 권한이 없음");

    private final HttpStatus status;
    private final String     code;
    private final String     message;

}
