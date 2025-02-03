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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {

    @PersistenceContext
    private final EntityManager entityManager;

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
        Member member = memberRepository.findByIdAndDisabled(dto.getMemberId(), false)
                                        .orElseThrow(() -> new MemberException());

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

        //모임 채팅방 엔티티 생성
        ChatRoom chatRoom = ChatRoom.builder()
                                    .group(group)
                                    .build();
        group.setChatRoom(chatRoom);
        chatRoomRepository.save(chatRoom);
        groupRepository.save(group);

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
        groupMembershipRepository.updateDisabledForAllGroupMembership(groupId,
                                                                      true); //해당 모임 ID를 갖는 멤버십 일괄 삭제(Soft Delete)
        entityManager.flush();
        entityManager.clear();  //벌크 연산 후 영속성 컨텍스트 초기화

        return group.getDisabled();
    }

    /**
     * 모임 가입 신청을 승인 또는 거절
     *
     * @param groupLeaderId - 모임 관리자 ID
     * @param groupId       - 모임 ID
     * @param memberId      - 모임 가입 신청 회원 ID
     * @param isAccept      - 가입 승인 여부
     * @return 모임 가입 승인 여부
     */
    @Transactional
    public boolean approveJoining(@NotNull @Min(1) final Long groupLeaderId,
                                  @NotNull @Min(1) final Long groupId,
                                  @NotNull @Min(1) final Long memberId,
                                  final boolean isAccept) {
        Group group = groupRepository.findByIdAndDisabled(groupId, false)
                                     .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

        //대상 모임이 모집 상태가 아닐 경우 예외(= 모집이 닫혀있는 경우 예외)
        if (group.getRecruitStatus() != RecruitStatus.RECRUITING)
            throw new GroupException(GroupErrorCode.GROUP_NOT_IN_RECRUITMENT_STATUS);

        //대상 모임 내 가입 회원 수 조회
        int count = groupMembershipRepository.countByGroupIdAndGroupRoleIn(groupId, Set.of(GroupRole.LEADER,
                                                                                           GroupRole.PARTICIPANT));

        //모임 최대 가입 한도와 가입 회원 수 비교, 이미 최대 가입 한도에 도달한 경우 예외
        if (group.getMaxRecruitCount() <= count)
            throw new GroupException(GroupErrorCode.GROUP_MAXIMUM_NUMBER_OF_MEMBERS);

        //모임 가입 신청을 승인하려는 회원이 해당 모임의 관리자(LEADER) 권한을 갖고 있는지 확인
        GroupMembership groupLeaderMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId,
                                                                                                              groupLeaderId,
                                                                                                              false)
                                                                         .orElseThrow(
                                                                                 () -> new GroupMembershipException(
                                                                                         GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND
                                                                                 )
                                                                         );
        if (groupLeaderMembership.getGroupRole() != GroupRole.LEADER)
            throw new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION);

        //모임 가입을 신청한 회원의 멤버십을 조회
        GroupMembership groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId,
                                                                                                        memberId,
                                                                                                        false)
                                                                   .orElseThrow(
                                                                           () -> new GroupMembershipException(
                                                                                   GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND
                                                                           )
                                                                   );

        //멤버십 상태가 미승인(PENDING) 또는 거절(REJECTED) 상태가 아닐 경우 예외(= 모임에 가입된 상태(APPROVED) 또는 탈퇴(LEAVE)한 상태의 경우 예외 발생)
        if (groupMembership.getStatus() == MembershipStatus.APPROVED
            || groupMembership.getStatus() == MembershipStatus.LEAVE)
            throw new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_UNACCEPTABLE_STATUS);

        //모임의 관리자 권한을 갖는 회원이 가입을 승인한 경우(isAccept = true)
        if (isAccept) {
            groupMembership.modifyStatus(MembershipStatus.APPROVED);
            return true;
        }

        //모임의 관리자 권한을 갖는 회원이 가입을 승인하지 않은 경우(isAccept = false)
        groupMembership.modifyStatus(MembershipStatus.REJECTED);
        return false;
    }

    /**
     * 모임 내 회원의 권한을 변경
     *
     * @param groupLeaderId - 모임 관리자 ID
     * @param groupId       - 모임 ID
     * @param memberId      - 모임 내 권한 변경 대상 회원 ID
     * @return 권한 변경 성공 여부
     */
    @Transactional
    public boolean modifyGroupRole(@NotNull @Min(1) final Long groupLeaderId,
                                   @NotNull @Min(1) final Long groupId,
                                   @NotNull @Min(1) final Long memberId) {
        //모임 내 회원의 권한을 변경하려는 회원이 해당 모임의 관리자(LEADER) 권한을 갖고 있는지 확인
        GroupMembership groupLeaderMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId,
                                                                                                              groupLeaderId,
                                                                                                              false)
                                                                         .orElseThrow(
                                                                                 () -> new GroupMembershipException(
                                                                                         GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND
                                                                                 )
                                                                         );
        if (groupLeaderMembership.getGroupRole() != GroupRole.LEADER)
            throw new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION);

        //모임 내 권한을 변경하려는 회원 멤버십 조회
        GroupMembership groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId,
                                                                                                        memberId,
                                                                                                        false)
                                                                   .orElseThrow(
                                                                           () -> new GroupMembershipException(
                                                                                   GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND
                                                                           )
                                                                   );

        //멤버십이 가입 상태(APPROVED)가 아닐 경우 예외(= 미승인(PENDING) 또는 거절(REJECTED) 또는 탈퇴(LEAVE)일 경우 예외)
        if (groupMembership.getStatus() != MembershipStatus.APPROVED)
            throw new GroupMembershipException(
                    GroupMembershipErrorCode.GROUP_MEMBERSHIP_GROUP_ROLE_NOT_CHANGEABLE_STATE
            );

        //회원의 모임 내 권한을 변경: LEADER <-> PARTICIPANT
        GroupRole groupRole = groupMembership.getGroupRole();
        if (groupRole == GroupRole.LEADER)
            groupMembership.modifyGroupRole(GroupRole.PARTICIPANT);
        else
            groupMembership.modifyGroupRole(GroupRole.LEADER);

        return true;
    }

    /**
     * 모임에서 탈퇴
     *
     * @param groupId  - 모임 ID
     * @param memberId - 회원 ID
     * @return 탈퇴 성공 여부
     */
    @Transactional
    public boolean leaveGroup(@NotNull @Min(1) final Long groupId, @NotNull @Min(1) final Long memberId) {
        GroupMembership groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId,
                                                                                                        memberId,
                                                                                                        false)
                                                                   .orElseThrow(
                                                                           () -> new GroupMembershipException(
                                                                                   GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND
                                                                           )
                                                                   );

        //모임을 탈퇴하기 전 모임 내 관리자 권한을 갖는 회원의 숫자 조회
        int groupLeaderCount = groupMembershipRepository.countByGroupIdAndGroupRoleAndDisabled(groupId,
                                                                                               GroupRole.LEADER,
                                                                                               false);

        //탈퇴하려는 회원이 관리자 권한을 갖고 있으며, 해당 모임 내 관리자 권한의 회원이 1명 이하인 경우 예외 발생, 탈퇴가 성공하려면 탈퇴 후 모임의 관리자가 1명 이상 존재해야 함
        if (groupMembership.getGroupRole() == GroupRole.LEADER && groupLeaderCount <= 1)
            throw new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_UNABLE_TO_LEAVE);

        groupMembership.modifyStatus(MembershipStatus.LEAVE);
        groupMembership.deactivate();

        return true;
    }

}
