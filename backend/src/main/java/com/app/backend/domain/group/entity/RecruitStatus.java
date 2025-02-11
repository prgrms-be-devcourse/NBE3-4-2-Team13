package com.app.backend.domain.group.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RecruitStatus {

    RECRUITING(false), CLOSED(false);

    private boolean forceStatus;

    RecruitStatus modifyForceStatus(final boolean newForceStatus) {
        if (forceStatus != newForceStatus)
            forceStatus = newForceStatus;
        return this;
    }

}
