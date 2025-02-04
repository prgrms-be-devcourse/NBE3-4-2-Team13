package com.app.backend.domain.group.dto.response;

import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.global.util.AppUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class GroupResponse {

    public static Detail toDetail(final Group group) {
        return Detail.builder()
                     .categoryName(group.getCategory().getName())
                     .name(group.getName())
                     .province(group.getProvince())
                     .city(group.getCity())
                     .town(group.getTown())
                     .description(group.getDescription())
                     .recruitStatus(group.getRecruitStatus().name())
                     .maxRecruitCount(group.getMaxRecruitCount())
                     .currentMemberCount(Math.toIntExact(
                             group.getMembers().stream().filter(m -> m.getStatus() == MembershipStatus.APPROVED
                                                                     && !m.getDisabled()).count()
                     )).createdAt(AppUtil.localDateTimeToString(group.getCreatedAt()))
                     .build();
    }

    public static ListInfo toListInfo(final Group group) {
        return ListInfo.builder()
                       .categoryName(group.getCategory().getName())
                       .name(group.getName())
                       .province(group.getProvince())
                       .city(group.getCity())
                       .town(group.getTown())
                       .recruitStatus(group.getRecruitStatus().name())
                       .maxRecruitCount(group.getMaxRecruitCount())
                       .currentMemberCount(Math.toIntExact(
                               group.getMembers().stream().filter(m -> m.getStatus() == MembershipStatus.APPROVED
                                                                       && !m.getDisabled()).count()
                       )).createdAt(AppUtil.localDateTimeToString(group.getCreatedAt()))
                       .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Detail {
        private final String  categoryName;
        private final String  name;
        private final String  province;
        private final String  city;
        private final String  town;
        private final String  description;
        private final String  recruitStatus;
        private final Integer maxRecruitCount;
        private final Integer currentMemberCount;
        private final String  createdAt;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ListInfo {
        private final String  categoryName;
        private final String  name;
        private final String  province;
        private final String  city;
        private final String  town;
        private final String  recruitStatus;
        private final Integer maxRecruitCount;
        private final Integer currentMemberCount;
        private final String  createdAt;
    }

}
