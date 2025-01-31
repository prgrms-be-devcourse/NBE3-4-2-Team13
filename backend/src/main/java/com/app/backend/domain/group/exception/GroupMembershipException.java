package com.app.backend.domain.group.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;

public class GroupMembershipException extends DomainException {
    public GroupMembershipException(final DomainErrorCode domainErrorCode) {
        super(domainErrorCode);
    }
}
