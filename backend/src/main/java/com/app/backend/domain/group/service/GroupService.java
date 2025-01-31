package com.app.backend.domain.group.service;

import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.domain.chat.room.repository.ChatRoomRepository;
import com.app.backend.domain.group.dto.request.GroupRequest;
import com.app.backend.domain.group.dto.response.GroupResponse;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupErrorCode;
import com.app.backend.domain.group.exception.GroupException;
import com.app.backend.domain.group.exception.GroupMembershipErrorCode;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.exception.MemberException;
import com.app.backend.domain.member.repository.MemberRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository           groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final MemberRepository          memberRepository;
    private final ChatRoomRepository        chatRoomRepository;

    /**
     * 모임(Group) 저장
     *
     * @param dto - 모임(Group) 생성 요청 DTO
     * @return 생성된 Group 엔티티 ID
     */
    @Transactional
    public Long createGroup(@NotNull final GroupRequest.Create dto) {
        //모임을 생성하는 회원 검증
        //TODO: MemberException 예외 코드, 메시지 추가 필요
//        Member member = memberRepository.findByIdAndDisabled(dto.getMemberId(), false)
//                                        .orElseThrow(() -> new MemberException());
        Member member = memberRepository.findById(dto.getMemberId())
                                        .orElseThrow(MemberException::new);
        if (member.isDisabled())
            throw new MemberException();

        //모임 엔티티 생성
        Group group = Group.builder()
                           .name(dto.getName())
                           .province(dto.getProvince())
                           .city(dto.getCity())
                           .town(dto.getTown())
                           .description(dto.getDescription())
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(dto.getMaxRecruitCount())
                           .build();
        groupRepository.save(group);

        //모임 채팅방 엔티티 생성
        ChatRoom chatRoom = ChatRoom.builder()
                                    .group(group)
                                    .build();
        chatRoomRepository.save(chatRoom);
        group.setChatRoom(chatRoom);

        //모임 멤버십 엔티티 생성(회원-모임 연결 테이블, 모임 관리자 권한(LEADER) 부여)
        GroupMembership groupMembership = GroupMembership.builder()
                                                         .member(member)
                                                         .group(group)
                                                         .groupRole(GroupRole.LEADER)
                                                         .build();
        groupMembershipRepository.save(groupMembership);

        return group.getId();
    }

    /**
     * 모임(Group) 단 건 조회
     *
     * @param groupId - 모임 ID
     * @return 모임 응답 DTO
     */
    public GroupResponse.Detail getGroup(@NotNull @Min(1) final Long groupId) {
        Group group = groupRepository.findByIdAndDisabled(groupId, false)
                                     .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));
        return GroupResponse.toDetail(group);
    }

    /**
     * 모임(Group) 다 건 조회
     *
     * @return 모임 응답 DTO 목록(List)
     */
    public List<GroupResponse.ListInfo> getGroups() {
        return groupRepository.findAllByDisabled(false).stream().map(GroupResponse::toListInfo).toList();
    }

    /**
     * 모임(Group) 다 건 조회
     *
     * @return 모임 응답 DTO 목록(Page)
     */
    public Page<GroupResponse.ListInfo> getGroups(@NotNull final Pageable pageable) {
        return groupRepository.findAllByDisabled(false, pageable).map(GroupResponse::toListInfo);
    }

    /**
     * 모임 이름으로 모임(Group) 다 건 조회
     *
     * @param name - 모임 이름
     * @return 모임 응답 DTO 목록(List)
     */
    public List<GroupResponse.ListInfo> getGroupsByNameContaining(final String name) {
        return groupRepository.findAllByNameContainingAndDisabled(name, false)
                              .stream()
                              .map(GroupResponse::toListInfo)
                              .toList();
    }

    /**
     * 모임 이름으로 모임(Group) 다 건 조회
     *
     * @param name     - 모임 이름
     * @param pageable - 페이징 객체
     * @return 모임 응답 DTO 목록(Page)
     */
    public Page<GroupResponse.ListInfo> getGroupsByNameContaining(final String name, @NotNull final Pageable pageable) {
        return groupRepository.findAllByNameContainingAndDisabled(name, false, pageable).map(GroupResponse::toListInfo);
    }

    /**
     * 상세 주소로 모임(Group) 다 건 조회
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @return 모임 응답 DTO 목록(List)
     */
    public List<GroupResponse.ListInfo> getGroupsByRegion(final String province, final String city, final String town) {
        return groupRepository.findAllByRegion(province, city, town, false)
                              .stream()
                              .map(GroupResponse::toListInfo)
                              .toList();
    }

    /**
     * 상세 주소로 모임(Group) 다 건 조회
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param pageable - 페이징 객체
     * @return 모임 응답 DTO 목록(Page)
     */
    public Page<GroupResponse.ListInfo> getGroupsByRegion(final String province,
                                                          final String city,
                                                          final String town,
                                                          @NotNull final Pageable pageable) {
        return groupRepository.findAllByRegion(province, city, town, false, pageable).map(GroupResponse::toListInfo);
    }

    /**
     * 모임 이름과 상세 주소로 모임(Group) 다 건 조회
     *
     * @param name     - 모임 이름
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @return 모임 응답 DTO 목록(List)
     */
    public List<GroupResponse.ListInfo> getGroupsByNameContainingAndRegion(final String name,
                                                                           final String province,
                                                                           final String city,
                                                                           final String town) {
        return groupRepository.findAllByNameContainingAndRegion(name, province, city, town, false)
                              .stream()
                              .map(GroupResponse::toListInfo)
                              .toList();
    }

    /**
     * 모임 이름과 상세 주소로 모임(Group) 다 건 조회
     *
     * @param name     - 모임 이름
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param pageable - 페이징 객체
     * @return 모임 응답 DTO 목록(Page)
     */
    public Page<GroupResponse.ListInfo> getGroupsByNameContainingAndRegion(final String name,
                                                                           final String province,
                                                                           final String city,
                                                                           final String town,
                                                                           @NotNull final Pageable pageable) {
        return groupRepository.findAllByNameContainingAndRegion(name, province, city, town, false, pageable)
                              .map(GroupResponse::toListInfo);
    }

    /**
     * 모임(Group) 수정
     *
     * @param dto - 모임(Group) 수정 요청 DTO
     * @return 모임 응답 DTO
     */
    @Transactional
    public GroupResponse.Detail modifyGroup(@NotNull final GroupRequest.Update dto) {
        GroupMembership groupMembership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(dto.getGroupId(),
                                                                              dto.getMemberId(),
                                                                              false)
                                         .orElseThrow(
                                                 () -> new GroupMembershipException(
                                                         GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND
                                                 )
                                         );

        //회원의 모임 내 권한 확인
        if (groupMembership.getGroupRole() != GroupRole.LEADER
            || groupMembership.getStatus() != MembershipStatus.APPROVED)
            throw new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION);

        Group group = groupRepository.findByIdAndDisabled(dto.getGroupId(), false)
                                     .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

        group.modifyName(dto.getName())
             .modifyRegion(dto.getProvince(), dto.getCity(), dto.getTown())
             .modifyDescription(dto.getDescription())
             .modifyRecruitStatus(RecruitStatus.valueOf(dto.getRecruitStatus()))
             .modifyMaxRecruitCount(dto.getMaxRecruitCount());

        return GroupResponse.toDetail(group);
    }

    /**
     * 모임(Group) 삭제(Soft Delete)
     *
     * @param groupId  - 모임 ID
     * @param memberId - 회원 ID
     * @return 모임 비활성화(disabled) 여부
     */
    @Transactional
    public boolean deleteGroup(@NotNull @Min(1) final Long groupId, @NotNull @Min(1) final Long memberId) {
        GroupMembership groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId,
                                                                                                        memberId,
                                                                                                        false)
                                                                   .orElseThrow(
                                                                           () -> new GroupMembershipException(
                                                                                   GroupMembershipErrorCode
                                                                                           .GROUP_MEMBERSHIP_NOT_FOUND
                                                                           )
                                                                   );

        //회원의 모임 내 권한 확인
        if (groupMembership.getGroupRole() != GroupRole.LEADER
            || groupMembership.getStatus() != MembershipStatus.APPROVED)
            throw new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION);

        Group group = groupRepository.findByIdAndDisabled(groupId, false)
                                     .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

        group.deactivate();
        //TODO: 모임에 가입한 회원, 채팅방 등 사후처리 필요

        return group.getDisabled();
    }

}
