package com.app.backend.domain.meetingApplication.service;

import static com.app.backend.domain.group.entity.GroupRole.PARTICIPANT;

import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.exception.GroupMembershipErrorCode;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationDto;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationListDto;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationReqBody;
import com.app.backend.domain.meetingApplication.dto.response.MeetingApplicationResponse;
import com.app.backend.domain.meetingApplication.dto.response.MeetingApplicationResponse.Detail;
import com.app.backend.domain.meetingApplication.entity.MeetingApplication;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationErrorCode;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationException;
import com.app.backend.domain.meetingApplication.repository.MeetingApplicationRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingApplicationService {
    private final MeetingApplicationRepository meetingApplicationRepository;
    private final GroupRepository              groupRepository;
    private final MemberRepository             memberRepository;
    private final GroupMembershipRepository    groupMembershipRepository;

    @Transactional
    public MeetingApplication create(Long groupId, MeetingApplicationReqBody request, Long memberId) {

        Group group = groupRepository.findByIdAndDisabled(groupId, false)
                                     .orElseThrow(() -> new MeetingApplicationException(
                                             MeetingApplicationErrorCode.GROUP_NOT_FOUND));
        Member member = memberRepository.findByIdAndDisabled(memberId, false)
                                        .orElseThrow(() -> new MeetingApplicationException(
                                                MeetingApplicationErrorCode.MEMBER_NOT_FOUND));

        // REJECTED, LEAVE 사용자의 groupMembership status 수정
        GroupMembership groupMembership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId, memberId, false)
                                         .map(existingMembership -> {
                                             if (existingMembership.getStatus() == MembershipStatus.REJECTED ||
                                                 existingMembership.getStatus() == MembershipStatus.LEAVE) {
                                                 existingMembership.modifyStatus(MembershipStatus.PENDING);
                                                 return groupMembershipRepository.save(existingMembership);// 상태 업데이트
                                             }
                                             throw new MeetingApplicationException(
                                                     MeetingApplicationErrorCode.ALREADY_IN_GROUP);
                                         })
                                         .orElseGet(() -> groupMembershipRepository.save( // 새로운 멤버십 생성
                                                                                          GroupMembership.builder()
                                                                                                         .group(group)
                                                                                                         .member(member)
                                                                                                         .groupRole(
                                                                                                                 PARTICIPANT)
                                                                                                         .build()
                                         ));

        return meetingApplicationRepository.findByGroup_IdAndMember_IdAndDisabled(groupId, memberId, false)
                                           .orElseGet(() -> {
                                               return meetingApplicationRepository.save(
                                                       MeetingApplication.builder()
                                                                         .context(request.context())
                                                                         .group(group)
                                                                         .member(member)
                                                                         .build()
                                               );
                                           }).modifyContext(request.context());
    }

    // 리스트 조회
    public MeetingApplicationListDto getMeetingApplications(Long groupId, Long memberId) {

        GroupMembership membership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId, memberId, false)
                                         .orElseThrow(() -> new MeetingApplicationException(
                                                 MeetingApplicationErrorCode.MEMBER_NOT_FOUND_IN_GROUP));

        // 조회 권한 체크
        if (!membership.getGroupRole().equals(GroupRole.LEADER)) {
            throw new MeetingApplicationException(MeetingApplicationErrorCode.UNAUTHORIZED_ACCESS);
        }

        List<MeetingApplicationDto> applications = meetingApplicationRepository.findByGroupIdAndDisabled(groupId, false)
                                                                               .stream()
                                                                               .map(MeetingApplicationDto::from)
                                                                               .collect(Collectors.toList());

        return new MeetingApplicationListDto(applications);
    }


    // 상세 조회
    public MeetingApplicationDto getMeetingApplication(Long groupId, Long meetingApplicationId, Long memberId) {

        GroupMembership membership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId, memberId, false)
                                         .orElseThrow(() -> new MeetingApplicationException(
                                                 MeetingApplicationErrorCode.MEMBER_NOT_FOUND_IN_GROUP));

        // 조회 권한 체크
        if (!membership.getGroupRole().equals(GroupRole.LEADER)) {
            throw new MeetingApplicationException(MeetingApplicationErrorCode.UNAUTHORIZED_ACCESS);
        }

        MeetingApplication meetingApplication =
                meetingApplicationRepository.findByGroupIdAndIdAndDisabled(groupId, meetingApplicationId, false)
                                            .orElseThrow(() -> new MeetingApplicationException(
                                                    MeetingApplicationErrorCode.MEETING_APPLICATION_NOT_FOUND));

        return MeetingApplicationDto.from(meetingApplication);
    }

    // 인원 제한 검증 메서드
    public void validateGroupMemberLimit(Long groupId) {
        Group group = groupRepository.findByIdAndDisabled(groupId, false)
                                     .orElseThrow(() -> new MeetingApplicationException(
                                             MeetingApplicationErrorCode.GROUP_NOT_FOUND));

        int approvedMemberCount = groupMembershipRepository.countByGroupIdAndStatusAndDisabled(
                groupId, MembershipStatus.APPROVED, false
        );

        if (approvedMemberCount >= group.getMaxRecruitCount()) {
            throw new MeetingApplicationException(MeetingApplicationErrorCode.GROUP_MEMBER_LIMIT_EXCEEDED);
        }
    }

    public MeetingApplicationResponse.Detail getMeetingApplicationById(Long groupId,
                                                                       Long meetingApplicationId,
                                                                       Long memberId) {
        GroupMembership groupLeaderMembership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId, memberId, false)
                                         .orElseThrow(() -> new GroupMembershipException(
                                                 GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND)
                                         );
        if (groupLeaderMembership.getStatus() != MembershipStatus.APPROVED
            || groupLeaderMembership.getGroupRole() != GroupRole.LEADER)
            throw new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION);

        MeetingApplication meetingApplication =
                meetingApplicationRepository.findByIdAndDisabled(meetingApplicationId, false)
                                            .orElseThrow(() -> new MeetingApplicationException(
                                                    MeetingApplicationErrorCode.MEETING_APPLICATION_NOT_FOUND
                                            ));
        GroupMembership groupMembership = groupMembershipRepository
                .findByGroupIdAndMemberIdAndDisabled(groupId, meetingApplication.getMember().getId(), false)
                .orElseThrow(() -> new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));

        return MeetingApplicationResponse.toDetail(meetingApplication,
                                                   groupMembership.getStatus() == MembershipStatus.REJECTED,
                                                   groupMembership.getStatus() == MembershipStatus.APPROVED,
                                                   groupMembership.getGroupRole() == GroupRole.LEADER);
    }

    public Page<MeetingApplicationResponse.Detail> getMeetingApplications(Long groupId,
                                                                          Long memberId,
                                                                          Pageable pageable) {
        GroupMembership groupMembership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId, memberId, false)
                                         .orElseThrow(() -> new GroupMembershipException(
                                                 GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND)
                                         );
        if (groupMembership.getStatus() != MembershipStatus.APPROVED
            || groupMembership.getGroupRole() != GroupRole.LEADER)
            throw new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION);

        List<MeetingApplication> list = meetingApplicationRepository.findByGroupIdAndDisabled(groupId, false);
        Map<Long, GroupMembership> map = groupMembershipRepository.findAllByGroupIdAndDisabled(groupId, false)
                                                                  .stream()
                                                                  .collect(
                                                                          Collectors.toMap(GroupMembership::getMemberId,
                                                                                           Function.identity())
                                                                  );

        List<MeetingApplicationResponse.Detail> content = list.stream()
                                                              .map(ma -> {
                                                                  Long            mId = ma.getMember().getId();
                                                                  GroupMembership gm  = map.get(mId);

                                                                  boolean rejected = gm != null
                                                                                     && gm.getStatus() ==
                                                                                        MembershipStatus.REJECTED;
                                                                  boolean isMember = gm != null
                                                                                     && gm.getStatus() ==
                                                                                        MembershipStatus.APPROVED;
                                                                  boolean isAdmin = isMember
                                                                                    && gm.getGroupRole() ==
                                                                                       GroupRole.LEADER;

                                                                  return MeetingApplicationResponse.toDetail(ma,
                                                                                                             rejected,
                                                                                                             isMember,
                                                                                                             isAdmin);
                                                              })
                                                              .sorted(Comparator.comparing(Detail::getCreatedAt)
                                                                                .reversed())
                                                              .toList();

        return new PageImpl<>(content, pageable, content.size());
    }
}
