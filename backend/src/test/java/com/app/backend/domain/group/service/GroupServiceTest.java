package com.app.backend.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.domain.group.dto.request.GroupRequest;
import com.app.backend.domain.group.dto.response.GroupResponse;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupErrorCode;
import com.app.backend.domain.group.exception.GroupException;
import com.app.backend.domain.group.exception.GroupMembershipErrorCode;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.supporter.SpringBootTestSupporter;
import com.app.backend.domain.member.entity.Member;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class GroupServiceTest extends SpringBootTestSupporter {

    @AfterEach
    void afterEach() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("[성공] 모임 저장")
    void createGroup() {
        //Given
        Member member = Member.builder()
                              .username("testUsername")
                              .password("testPassword")
                              .nickname("testNickname")
                              .build();
        em.persist(member);
        Long memberId = member.getId();

        GroupRequest.Create request = GroupRequest.Create.builder()
                                                         .name("test")
                                                         .province("test province")
                                                         .city("test city")
                                                         .town("test town")
                                                         .description("test description")
                                                         .maxRecruitCount(10)
                                                         .build();
        request.setMemberId(memberId);

        //When
        Long id = groupService.createGroup(request);
        afterEach();

        //Then
        Group           savedGroup      = em.find(Group.class, id);
        ChatRoom        chatRoom        = chatRoomRepository.findAll().stream().findFirst().get();
        GroupMembership groupMembership = groupMembershipRepository.findByGroupIdAndMemberId(id, memberId).get();

        assertThat(savedGroup.getName()).isEqualTo(request.getName());
        assertThat(savedGroup.getProvince()).isEqualTo(request.getProvince());
        assertThat(savedGroup.getCity()).isEqualTo(request.getCity());
        assertThat(savedGroup.getTown()).isEqualTo(request.getTown());
        assertThat(savedGroup.getDescription()).isEqualTo(request.getDescription());
        assertThat(savedGroup.getRecruitStatus()).isEqualTo(RecruitStatus.RECRUITING);
        assertThat(savedGroup.getMaxRecruitCount()).isEqualTo(request.getMaxRecruitCount());
        assertThat(groupMembership.getMemberId()).isEqualTo(memberId);
        assertThat(savedGroup.getChatRoom()).isEqualTo(chatRoom);
    }

    @Test
    @DisplayName("[성공] ID로 Group Detail DTO 조회")
    void getGroup() {
        //Given
        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long id = group.getId();
        afterEach();

        //When
        GroupResponse.Detail response = groupService.getGroup(id);

        //Then
        assertThat(response.getName()).isEqualTo(group.getName());
        assertThat(response.getProvince()).isEqualTo(group.getProvince());
        assertThat(response.getCity()).isEqualTo(group.getCity());
        assertThat(response.getTown()).isEqualTo(group.getTown());
        assertThat(response.getDescription()).isEqualTo(group.getDescription());
        assertThat(response.getRecruitStatus()).isEqualTo(RecruitStatus.RECRUITING.name());
        assertThat(response.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 ID로 Group Detail DTO 조회 시도")
    void getGroup_unknownId() {
        //Given
        Long unknownId = 1234567890L;

        //When

        //Then
        GroupErrorCode errorCode = GroupErrorCode.GROUP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.getGroup(unknownId))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[성공] 모든 Group ListInfo DTO 목록 조회")
    void getGroupList() {
        //Given
        int         size   = 20;
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Group group = Group.builder()
                               .name("test%d".formatted(i))
                               .province("test province%d".formatted(i))
                               .city("test city%d".formatted(i))
                               .town("test town%d".formatted(i))
                               .description("test description%d".formatted(i))
                               .recruitStatus(RecruitStatus.RECRUITING)
                               .maxRecruitCount(10)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        //When
        List<GroupResponse.ListInfo> responseList = groupService.getGroups();

        //Then
        assertThat(responseList).hasSize(size);
        for (int i = 0; i < size; i++) {
            Group                  group       = groups.get(i);
            GroupResponse.ListInfo responseDto = responseList.get(i);

            assertThat(responseDto.getName()).isEqualTo(group.getName());
            assertThat(responseDto.getProvince()).isEqualTo(group.getProvince());
            assertThat(responseDto.getCity()).isEqualTo(group.getCity());
            assertThat(responseDto.getTown()).isEqualTo(group.getTown());
            assertThat(responseDto.getRecruitStatus()).isEqualTo(group.getRecruitStatus().name());
            assertThat(responseDto.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 모든 Group ListInfo DTO 페이징 목록 조회")
    void getGroupPage() {
        //Given
        int         size   = 20;
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Group group = Group.builder()
                               .name("test%d".formatted(i))
                               .province("test province%d".formatted(i))
                               .city("test city%d".formatted(i))
                               .town("test town%d".formatted(i))
                               .description("test description%d".formatted(i))
                               .recruitStatus(RecruitStatus.RECRUITING)
                               .maxRecruitCount(10)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        Pageable pageable = PageRequest.of(0, 10);

        //When
        Page<GroupResponse.ListInfo> responsePage = groupService.getGroups(pageable);

        //Then
        List<GroupResponse.ListInfo> responseList = responsePage.getContent();
        groups = groups.subList(0, pageable.getPageSize());

        assertThat(responseList).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < groups.size(); i++) {
            Group                  group       = groups.get(i);
            GroupResponse.ListInfo responseDto = responseList.get(i);

            assertThat(responseDto.getName()).isEqualTo(group.getName());
            assertThat(responseDto.getProvince()).isEqualTo(group.getProvince());
            assertThat(responseDto.getCity()).isEqualTo(group.getCity());
            assertThat(responseDto.getTown()).isEqualTo(group.getTown());
            assertThat(responseDto.getRecruitStatus()).isEqualTo(group.getRecruitStatus().name());
            assertThat(responseDto.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름으로 ListInfo DTO 목록 조회")
    void getGroupsByNameContainingList() {
        //Given
        int         size   = 20;
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Group group = Group.builder()
                               .name("test%d".formatted(i))
                               .province("test province%d".formatted(i))
                               .city("test city%d".formatted(i))
                               .town("test town%d".formatted(i))
                               .description("test description%d".formatted(i))
                               .recruitStatus(RecruitStatus.RECRUITING)
                               .maxRecruitCount(10)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        String name = "5";

        //When
        List<GroupResponse.ListInfo> responseList = groupService.getGroupsByNameContaining(name);

        //Then
        groups = groups.stream().filter(group -> group.getName().contains(name)).toList();

        assertThat(responseList).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group                  group       = groups.get(i);
            GroupResponse.ListInfo responseDto = responseList.get(i);

            assertThat(responseDto.getName()).isEqualTo(group.getName());
            assertThat(responseDto.getProvince()).isEqualTo(group.getProvince());
            assertThat(responseDto.getCity()).isEqualTo(group.getCity());
            assertThat(responseDto.getTown()).isEqualTo(group.getTown());
            assertThat(responseDto.getRecruitStatus()).isEqualTo(group.getRecruitStatus().name());
            assertThat(responseDto.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름으로 ListInfo DTO 페이징 목록 조회")
    void getGroupsByNameContainingPage() {
        //Given
        int         size   = 20;
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Group group = Group.builder()
                               .name("test%d".formatted(i))
                               .province("test province%d".formatted(i))
                               .city("test city%d".formatted(i))
                               .town("test town%d".formatted(i))
                               .description("test description%d".formatted(i))
                               .recruitStatus(RecruitStatus.RECRUITING)
                               .maxRecruitCount(10)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        Pageable pageable = PageRequest.of(0, 10);
        String   name     = "5";

        //When
        Page<GroupResponse.ListInfo> responsePage = groupService.getGroupsByNameContaining(name, pageable);

        //Then
        List<GroupResponse.ListInfo> responseList = responsePage.getContent();
        groups = groups.stream().filter(group -> group.getName().contains(name)).limit(pageable.getPageSize()).toList();

        assertThat(responseList).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group                  group       = groups.get(i);
            GroupResponse.ListInfo responseDto = responseList.get(i);

            assertThat(responseDto.getName()).isEqualTo(group.getName());
            assertThat(responseDto.getProvince()).isEqualTo(group.getProvince());
            assertThat(responseDto.getCity()).isEqualTo(group.getCity());
            assertThat(responseDto.getTown()).isEqualTo(group.getTown());
            assertThat(responseDto.getRecruitStatus()).isEqualTo(group.getRecruitStatus().name());
            assertThat(responseDto.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 주소로 ListInfo DTO 목록 조회")
    void getGroupsByRegionList() {
        //Given
        int         size   = 20;
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Group group = Group.builder()
                               .name("test%d".formatted(i))
                               .province("test province%d".formatted(i))
                               .city("test city%d".formatted(i))
                               .town("test town%d".formatted(i))
                               .description("test description%d".formatted(i))
                               .recruitStatus(RecruitStatus.RECRUITING)
                               .maxRecruitCount(10)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        String province = "test province10";
        String city     = "test city10";
        String town     = "test town10";

        //When
        List<GroupResponse.ListInfo> responseList = groupService.getGroupsByRegion(province, city, town);

        //Then
        groups = groups.stream().filter(group -> group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(responseList).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group                  group       = groups.get(i);
            GroupResponse.ListInfo responseDto = responseList.get(i);

            assertThat(responseDto.getName()).isEqualTo(group.getName());
            assertThat(responseDto.getProvince()).isEqualTo(group.getProvince());
            assertThat(responseDto.getCity()).isEqualTo(group.getCity());
            assertThat(responseDto.getTown()).isEqualTo(group.getTown());
            assertThat(responseDto.getRecruitStatus()).isEqualTo(group.getRecruitStatus().name());
            assertThat(responseDto.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 주소로 ListInfo DTO 페이징 목록 조회")
    void getGroupsByRegionPage() {
        //Given
        int         size   = 20;
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Group group = Group.builder()
                               .name("test%d".formatted(i))
                               .province("test province%d".formatted(i))
                               .city("test city%d".formatted(i))
                               .town("test town%d".formatted(i))
                               .description("test description%d".formatted(i))
                               .recruitStatus(RecruitStatus.RECRUITING)
                               .maxRecruitCount(10)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        Pageable pageable = PageRequest.of(0, 10);

        String province = "test province10";
        String city     = "test city10";
        String town     = "test town10";

        //When
        Page<GroupResponse.ListInfo> responsePage = groupService.getGroupsByRegion(province, city, town, pageable);

        //Then
        groups = groups.stream().filter(group -> group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();
        List<GroupResponse.ListInfo> responseList = responsePage.getContent();

        assertThat(responseList).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < groups.size(); i++) {
            Group                  group       = groups.get(i);
            GroupResponse.ListInfo responseDto = responseList.get(i);

            assertThat(responseDto.getName()).isEqualTo(group.getName());
            assertThat(responseDto.getProvince()).isEqualTo(group.getProvince());
            assertThat(responseDto.getCity()).isEqualTo(group.getCity());
            assertThat(responseDto.getTown()).isEqualTo(group.getTown());
            assertThat(responseDto.getRecruitStatus()).isEqualTo(group.getRecruitStatus().name());
            assertThat(responseDto.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름과 주소로 ListInfo DTO 목록 조회")
    void getGroupsByNameContainingAndRegionList() {
        //Given
        int         size   = 20;
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Group group = Group.builder()
                               .name("test%d".formatted(i))
                               .province("test province%d".formatted(i))
                               .city("test city%d".formatted(i))
                               .town("test town%d".formatted(i))
                               .description("test description%d".formatted(i))
                               .recruitStatus(RecruitStatus.RECRUITING)
                               .maxRecruitCount(10)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        String name = "1";

        String province = "test province10";
        String city     = "test city10";
        String town     = "test town10";

        //When
        List<GroupResponse.ListInfo> responseList = groupService.getGroupsByNameContainingAndRegion(name,
                                                                                                    province,
                                                                                                    city,
                                                                                                    town);

        //Then
        groups = groups.stream().filter(group -> group.getName().contains(name)
                                                 && group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(responseList).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group                  group       = groups.get(i);
            GroupResponse.ListInfo responseDto = responseList.get(i);

            assertThat(responseDto.getName()).isEqualTo(group.getName());
            assertThat(responseDto.getProvince()).isEqualTo(group.getProvince());
            assertThat(responseDto.getCity()).isEqualTo(group.getCity());
            assertThat(responseDto.getTown()).isEqualTo(group.getTown());
            assertThat(responseDto.getRecruitStatus()).isEqualTo(group.getRecruitStatus().name());
            assertThat(responseDto.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름과 주소로 ListInfo DTO 페이징 목록 조회")
    void getGroupsByNameContainingAndRegionPage() {
        //Given
        int         size   = 20;
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Group group = Group.builder()
                               .name("test%d".formatted(i))
                               .province("test province%d".formatted(i))
                               .city("test city%d".formatted(i))
                               .town("test town%d".formatted(i))
                               .description("test description%d".formatted(i))
                               .recruitStatus(RecruitStatus.RECRUITING)
                               .maxRecruitCount(10)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        Pageable pageable = PageRequest.of(0, 10);

        String name = "1";

        String province = "test province10";
        String city     = "test city10";
        String town     = "test town10";

        //When
        Page<GroupResponse.ListInfo> responsePage = groupService.getGroupsByNameContainingAndRegion(name,
                                                                                                    province,
                                                                                                    city,
                                                                                                    town,
                                                                                                    pageable);

        //Then
        groups = groups.stream().filter(group -> group.getName().contains(name)
                                                 && group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();
        List<GroupResponse.ListInfo> responseList = responsePage.getContent();

        assertThat(responseList).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < groups.size(); i++) {
            Group                  group       = groups.get(i);
            GroupResponse.ListInfo responseDto = responseList.get(i);

            assertThat(responseDto.getName()).isEqualTo(group.getName());
            assertThat(responseDto.getProvince()).isEqualTo(group.getProvince());
            assertThat(responseDto.getCity()).isEqualTo(group.getCity());
            assertThat(responseDto.getTown()).isEqualTo(group.getTown());
            assertThat(responseDto.getRecruitStatus()).isEqualTo(group.getRecruitStatus().name());
            assertThat(responseDto.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] ID로 모임 조회 후 값 수정")
    void modifyGroup() {
        //Given
        Member member = Member.builder()
                              .username("testUsername")
                              .password("testPassword")
                              .nickname("testNickname")
                              .build();
        em.persist(member);
        Long memberId = member.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupMembership = GroupMembership.builder()
                                                         .member(member)
                                                         .group(group)
                                                         .groupRole(GroupRole.LEADER)
                                                         .build();
        em.persist(groupMembership);
        afterEach();

        GroupRequest.Update update = GroupRequest.Update.builder()
                                                        .name("new test")
                                                        .province("new test province")
                                                        .city("new test city")
                                                        .town("new test town")
                                                        .description("new test description")
                                                        .recruitStatus(RecruitStatus.CLOSED.name())
                                                        .maxRecruitCount(20)
                                                        .build();
        update.setGroupId(groupId);
        update.setMemberId(memberId);

        //When
        GroupResponse.Detail response = groupService.modifyGroup(update);

        //Then
        assertThat(response.getName()).isNotEqualTo(group.getName());
        assertThat(response.getName()).isEqualTo(update.getName());
        assertThat(response.getProvince()).isNotEqualTo(group.getProvince());
        assertThat(response.getProvince()).isEqualTo(update.getProvince());
        assertThat(response.getCity()).isNotEqualTo(group.getCity());
        assertThat(response.getCity()).isEqualTo(update.getCity());
        assertThat(response.getTown()).isNotEqualTo(group.getTown());
        assertThat(response.getTown()).isEqualTo(update.getTown());
        assertThat(response.getDescription()).isNotEqualTo(group.getDescription());
        assertThat(response.getDescription()).isEqualTo(update.getDescription());
        assertThat(response.getRecruitStatus()).isNotEqualTo(RecruitStatus.RECRUITING.name());
        assertThat(response.getRecruitStatus()).isEqualTo(RecruitStatus.CLOSED.name());
        assertThat(response.getMaxRecruitCount()).isNotEqualTo(group.getMaxRecruitCount());
        assertThat(response.getMaxRecruitCount()).isEqualTo(update.getMaxRecruitCount());
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 ID로 모임 조회 후 값 수정 시도")
    void modifyGroup_unknownId() {
        //Given
        Long unknownId = 1234567890L;

        GroupRequest.Update update = GroupRequest.Update.builder()
                                                        .groupId(unknownId)
                                                        .memberId(unknownId)
                                                        .name("new test")
                                                        .province("new test province")
                                                        .city("new test city")
                                                        .town("new test town")
                                                        .description("new test description")
                                                        .recruitStatus(RecruitStatus.CLOSED.name())
                                                        .maxRecruitCount(20)
                                                        .build();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.modifyGroup(update))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[성공] ID로 모임 삭제(Soft Delete)")
    void deleteGroup() {
        //Given
        Member member = Member.builder()
                              .username("testUsername")
                              .password("testPassword")
                              .nickname("testNickname")
                              .build();
        em.persist(member);
        Long memberId = member.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupMembership = GroupMembership.builder()
                                                         .member(member)
                                                         .group(group)
                                                         .groupRole(GroupRole.LEADER)
                                                         .build();
        em.persist(groupMembership);
        afterEach();

        //When
        boolean flag = groupService.deleteGroup(groupId, memberId);

        //Then
        Group deletedGroup = em.find(Group.class, groupId);
        GroupMembership deletedGroupMembership = em.find(GroupMembership.class,
                                                         GroupMembershipId.builder()
                                                                          .groupId(groupId)
                                                                          .memberId(memberId)
                                                                          .build());

        assertThat(flag).isTrue();
        assertThat(deletedGroup.getDisabled()).isTrue();
        assertThat(deletedGroupMembership.getDisabled()).isTrue();
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 ID로 모임 조회 후 삭제 시도")
    void deleteGroup_unknownId() {
        //Given
        Long unknownId = 1234567890L;

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.deleteGroup(unknownId, unknownId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[성공] Group ID와 Member ID 2개(모임 관리자, 모임 참가 신청자)로 모임 신청을 허가")
    void approveJoining_acceptTrue() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupJoiningMembership = GroupMembership.builder()
                                                                .member(memberParticipant)
                                                                .group(group)
                                                                .groupRole(GroupRole.PARTICIPANT)
                                                                .build();
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);
        afterEach();

        //When
        boolean flag = groupService.approveJoining(leaderId, groupId, joiningId, true);
        afterEach();

        //Then
        GroupMembership updatedGroupMembership = em.find(GroupMembership.class, GroupMembershipId.builder()
                                                                                                 .memberId(joiningId)
                                                                                                 .groupId(groupId)
                                                                                                 .build());

        assertThat(flag).isTrue();
        assertThat(updatedGroupMembership.getStatus()).isEqualTo(MembershipStatus.APPROVED);
    }

    @Test
    @DisplayName("[성공] Group ID와 Member ID 2개(모임 관리자, 모임 참가 신청자)로 모임 신청을 거부")
    void approveJoining_acceptFalse() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupJoiningMembership = GroupMembership.builder()
                                                                .member(memberParticipant)
                                                                .group(group)
                                                                .groupRole(GroupRole.PARTICIPANT)
                                                                .build();
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);
        afterEach();

        //When
        boolean flag = groupService.approveJoining(leaderId, groupId, joiningId, false);
        afterEach();

        //Then
        GroupMembership updatedGroupMembership = em.find(GroupMembership.class, GroupMembershipId.builder()
                                                                                                 .memberId(joiningId)
                                                                                                 .groupId(groupId)
                                                                                                 .build());

        assertThat(flag).isFalse();
        assertThat(updatedGroupMembership.getStatus()).isEqualTo(MembershipStatus.REJECTED);
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Group ID로 모임 신청 허가/거부 시도")
    void approveJoining_unknownGroupId() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();
        afterEach();

        Long unknownGroupId = 1234567890L;

        //When

        //Then
        GroupErrorCode errorCode = GroupErrorCode.GROUP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.approveJoining(leaderId, unknownGroupId, joiningId, true))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임의 모집 상태가 닫혀있을 때 모임 신청 허가/거부 시도")
    void approveJoining_closedGroup() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupJoiningMembership = GroupMembership.builder()
                                                                .member(memberParticipant)
                                                                .group(group)
                                                                .groupRole(GroupRole.PARTICIPANT)
                                                                .build();
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);

        group.modifyRecruitStatus(RecruitStatus.CLOSED);
        afterEach();

        //When

        //Then
        GroupErrorCode errorCode = GroupErrorCode.GROUP_NOT_IN_RECRUITMENT_STATUS;

        assertThatThrownBy(() -> groupService.approveJoining(leaderId, groupId, joiningId, true))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임이 최대 가입 한도에 도달한 상태에서 모임 신청 허가/거부 시도")
    void approveJoining_maximumMembers() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(1)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupJoiningMembership = GroupMembership.builder()
                                                                .member(memberParticipant)
                                                                .group(group)
                                                                .groupRole(GroupRole.PARTICIPANT)
                                                                .build();
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);
        afterEach();

        //When

        //Then
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MAXIMUM_NUMBER_OF_MEMBERS;

        assertThatThrownBy(() -> groupService.approveJoining(leaderId, groupId, joiningId, true))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 가입 신청 승인/거절을 모임의 관리자 권한이 없는 회원이 시도")
    void approveJoining_notALeader() {
        //Given
        Member memberFakeLeader = Member.builder()
                                        .username("testUsernameLeader")
                                        .password("testPasswordLeader")
                                        .nickname("testNicknameLeader")
                                        .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberFakeLeader);
        em.persist(memberParticipant);
        Long fakeLeaderId = memberFakeLeader.getId();
        Long joiningId    = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupFakeLeaderMembership = GroupMembership.builder()
                                                                   .member(memberFakeLeader)
                                                                   .group(group)
                                                                   .groupRole(GroupRole.PARTICIPANT)
                                                                   .build();
        GroupMembership groupJoiningMembership = GroupMembership.builder()
                                                                .member(memberParticipant)
                                                                .group(group)
                                                                .groupRole(GroupRole.PARTICIPANT)
                                                                .build();
        em.persist(groupFakeLeaderMembership);
        em.persist(groupJoiningMembership);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION;

        assertThatThrownBy(() -> groupService.approveJoining(fakeLeaderId, groupId, joiningId, true))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 가입을 희망하는 회원이 이미 가입/탈퇴한 상태인 경우")
    void approveJoining_unacceptableStatus() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupJoiningMembership = GroupMembership.builder()
                                                                .member(memberParticipant)
                                                                .group(group)
                                                                .groupRole(GroupRole.PARTICIPANT)
                                                                .build();
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);

        groupJoiningMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_UNACCEPTABLE_STATUS;

        assertThatThrownBy(() -> groupService.approveJoining(leaderId, groupId, joiningId, true))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[성공] 모임의 관리자가 모임 내 특정 회원의 권한을 변경(LEADER <-> PARTICIPANT)")
    void modifyGroupRole() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId = memberLeader.getId();
        Long memberId = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupMembership = GroupMembership.builder()
                                                         .member(memberParticipant)
                                                         .group(group)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);

        groupMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        //When
        boolean flag = groupService.modifyGroupRole(leaderId, groupId, memberId);
        afterEach();

        //Then
        GroupMembership updatedGroupMembership = em.find(GroupMembership.class, GroupMembershipId.builder()
                                                                                                 .groupId(groupId)
                                                                                                 .memberId(memberId)
                                                                                                 .build());

        assertThat(updatedGroupMembership.getGroupRole()).isNotEqualTo(GroupRole.PARTICIPANT);
        assertThat(updatedGroupMembership.getGroupRole()).isEqualTo(GroupRole.LEADER);
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Group ID로 회원 권한 변경 시도")
    void modifyGroupRole_unknownGroupId() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId = memberLeader.getId();
        Long memberId = memberParticipant.getId();

        Long unknownGroupId = 1234567890L;
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.modifyGroupRole(leaderId, unknownGroupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Member ID(모임 관리자)로 회원 권한 변경 시도")
    void modifyGroupRole_unknownLeaderId() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long memberId = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupMembership = GroupMembership.builder()
                                                         .member(memberParticipant)
                                                         .group(group)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);

        groupMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        Long unknownLeaderId = 1234567890L;

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.modifyGroupRole(unknownLeaderId, groupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 내 회원 권한 변경을 모임의 관리자 권한이 없는 회원이 시도")
    void modifyGroupRole_notALeader() {
        //Given
        Member memberFakeLeader = Member.builder()
                                        .username("testUsernameLeader")
                                        .password("testPasswordLeader")
                                        .nickname("testNicknameLeader")
                                        .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberFakeLeader);
        em.persist(memberParticipant);
        Long fakeLeaderId = memberFakeLeader.getId();
        Long memberId     = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupFakeLeaderMembership = GroupMembership.builder()
                                                                   .member(memberFakeLeader)
                                                                   .group(group)
                                                                   .groupRole(GroupRole.PARTICIPANT)
                                                                   .build();
        GroupMembership groupMembership = GroupMembership.builder()
                                                         .member(memberParticipant)
                                                         .group(group)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupFakeLeaderMembership);
        em.persist(groupMembership);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION;

        assertThatThrownBy(() -> groupService.modifyGroupRole(fakeLeaderId, groupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Member ID(모임 회원)의 회원 권한 변경 시도")
    void modifyGroupRole_unknownMemberId() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId = memberLeader.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupMembership = GroupMembership.builder()
                                                         .member(memberParticipant)
                                                         .group(group)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);

        groupMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        Long unknownMemberId = 1234567890L;

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.modifyGroupRole(leaderId, groupId, unknownMemberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 가입 상태가 아닌 회원의 모임 권한을 변경 시도")
    void modifyGroupRole_notChangeableState() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId = memberLeader.getId();
        Long memberId = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupMembership = GroupMembership.builder()
                                                         .member(memberParticipant)
                                                         .group(group)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_GROUP_ROLE_NOT_CHANGEABLE_STATE;

        assertThatThrownBy(() -> groupService.modifyGroupRole(leaderId, groupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[성공] 모임에서 탈퇴")
    void leaveGroup() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long memberId = memberParticipant.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        GroupMembership groupMembership = GroupMembership.builder()
                                                         .member(memberParticipant)
                                                         .group(group)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);

        groupMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        //When
        boolean flag = groupService.leaveGroup(groupId, memberId);
        afterEach();

        //Then
        GroupMembership leaveGroupMembership = em.find(GroupMembership.class, GroupMembershipId.builder()
                                                                                               .groupId(groupId)
                                                                                               .memberId(memberId)
                                                                                               .build());

        assertThat(flag).isTrue();
        assertThat(leaveGroupMembership.getStatus()).isEqualTo(MembershipStatus.LEAVE);
        assertThat(leaveGroupMembership.getDisabled()).isTrue();
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Group ID로 모임 탈퇴 시도")
    void leaveGroup_unknownGroupId() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        Member memberParticipant = Member.builder()
                                         .username("testUsernameParticipant")
                                         .password("testPasswordParticipant")
                                         .nickname("testNicknameParticipant")
                                         .build();
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long memberId = memberParticipant.getId();
        afterEach();

        Long unknownGroupId = 1234567890L;

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.leaveGroup(unknownGroupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Member ID로 모임 탈퇴 시도")
    void leaveGroup_unknownMemberId() {
        //Given
        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();
        afterEach();

        Long unknownMemberId = 1234567890L;

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.leaveGroup(groupId, unknownMemberId))
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 관리자가 1명뿐인 모임에서 관리자가 탈퇴를 시도하는 경우")
    void leaveGroup_unableToLeave() {
        //Given
        Member memberLeader = Member.builder()
                                    .username("testUsernameLeader")
                                    .password("testPasswordLeader")
                                    .nickname("testNicknameLeader")
                                    .build();
        em.persist(memberLeader);
        Long leaderId = memberLeader.getId();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                               .member(memberLeader)
                                                               .group(group)
                                                               .groupRole(GroupRole.LEADER)
                                                               .build();
        em.persist(groupLeaderMembership);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_UNABLE_TO_LEAVE;

        assertThatThrownBy(() -> groupService.leaveGroup(groupId, leaderId))
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

}
