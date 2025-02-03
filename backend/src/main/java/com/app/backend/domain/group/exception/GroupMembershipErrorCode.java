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
    GROUP_MEMBERSHIP_NO_PERMISSION(HttpStatus.FORBIDDEN, "GM002", "모임 수정 권한이 없음"),
    GROUP_MEMBERSHIP_UNACCEPTABLE_STATUS(HttpStatus.BAD_REQUEST, "GM003", "모임 가입 신청을 승인할 수 없음"),
    GROUP_MEMBERSHIP_GROUP_ROLE_NOT_CHANGEABLE_STATE(HttpStatus.BAD_REQUEST, "GM004", "모임 내 회원 권한을 변경할 수 없음"),
    GROUP_MEMBERSHIP_UNABLE_TO_LEAVE(HttpStatus.BAD_REQUEST, "GM005", "모임에 탈퇴할 수 없음");

    private final HttpStatus status;
    private final String     code;
    private final String     message;

}
