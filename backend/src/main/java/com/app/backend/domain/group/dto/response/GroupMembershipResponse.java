package com.app.backend.domain.group.dto.response;

import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.global.util.AppUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class GroupMembershipResponse {

    public static Detail toDetail(final GroupMembership groupMembership) {
        return Detail.builder()
                     .groupId(groupMembership.getGroupId())
                     .categoryName(groupMembership.getGroup().getCategory().getName())
                     .name(groupMembership.getGroup().getName())
                     .modifiedAt(AppUtil.localDateTimeToString(groupMembership.getModifiedAt()))
                     .isApplying(groupMembership.getStatus() == MembershipStatus.PENDING ? true : null)
                     .isRejected(groupMembership.getStatus() == MembershipStatus.REJECTED ? true : null)
                     .isMember(groupMembership.getStatus() == MembershipStatus.APPROVED ? true : null)
                     .isAdmin(groupMembership.getStatus() == MembershipStatus.APPROVED
                              && groupMembership.getGroupRole() == GroupRole.LEADER)
                     .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Detail {
        private Long    groupId;
        private String  categoryName;
        private String  name;
        private String  modifiedAt;
        private Boolean isApplying;
        private Boolean isRejected;
        private Boolean isMember;
        private Boolean isAdmin;
    }

}
