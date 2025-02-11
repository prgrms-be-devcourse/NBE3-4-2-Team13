package com.app.backend.domain.meetingApplication.dto.response;

import com.app.backend.domain.meetingApplication.entity.MeetingApplication;
import com.app.backend.global.util.AppUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class MeetingApplicationResponse {

    public static Detail toDetail(final MeetingApplication meetingApplication,
                                  final boolean rejected,
                                  final boolean isMember,
                                  final boolean isAdmin) {
        return Detail.builder()
                     .id(meetingApplication.getId())
                     .groupId(meetingApplication.getGroup().getId())
                     .memberId(meetingApplication.getMember().getId())
                     .nickname(meetingApplication.getMember().getNickname())
                     .content(meetingApplication.getContext())
                     .createdAt(AppUtil.localDateTimeToString(meetingApplication.getCreatedAt()))
                     .rejected(rejected)
                     .isMember(isMember)
                     .isAdmin(isAdmin)
                     .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Detail {
        private final Long    id;
        private final Long    groupId;
        private final Long    memberId;
        private final String  nickname;
        private final String  content;
        private final String  createdAt;
        private final Boolean rejected;
        private final Boolean isMember;
        private final Boolean isAdmin;
    }

}
