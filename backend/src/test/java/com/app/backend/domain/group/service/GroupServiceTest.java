package com.app.backend.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.domain.group.dto.request.GroupRequest;
import com.app.backend.domain.group.dto.response.GroupResponse;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
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

        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);
        String categoryName = category.getName();

        GroupRequest.Create request = GroupRequest.Create.builder()
                                                         .name("test")
                                                         .province("test province")
                                                         .city("test city")
                                                         .town("test town")
                                                         .description("test description")
                                                         .maxRecruitCount(10)
                                                         .categoryName(categoryName)
                                                         .build();

        //When
        Long id = groupService.createGroup(memberId, request);
        afterEach();

        //Then
        Group           savedGroup      = em.find(Group.class, id);
        ChatRoom        chatRoom        = chatRoomRepository.findAll().stream().findFirst().get();
        Category        findCategory    = categoryRepository.findAll().stream().findFirst().get();
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
        assertThat(savedGroup.getCategory()).isEqualTo(findCategory);
    }

    @Test
    @DisplayName("[성공] ID로 Group Detail DTO 조회")
    void getGroup() {
        //Given
        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .category(category)
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
        assertThat(response.getCategoryName()).isEqualTo(group.getCategory().getName());
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
        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

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
                               .category(category)
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
            assertThat(responseDto.getCategoryName()).isEqualTo(category.getName());
        }
    }

    @Test
    @DisplayName("[성공] 모든 Group ListInfo DTO 페이징 목록 조회")
    void getGroupPage() {
        //Given
        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

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
                               .category(category)
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
            assertThat(responseDto.getCategoryName()).isEqualTo(category.getName());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름으로 ListInfo DTO 목록 조회")
    void getGroupsByNameContainingList() {
        //Given
        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

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
                               .category(category)
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
            assertThat(responseDto.getCategoryName()).isEqualTo(category.getName());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름으로 ListInfo DTO 페이징 목록 조회")
    void getGroupsByNameContainingPage() {
        //Given
        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

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
                               .category(category)
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
            assertThat(responseDto.getCategoryName()).isEqualTo(category.getName());
        }
    }

    @Test
    @DisplayName("[성공] 주소로 ListInfo DTO 목록 조회")
    void getGroupsByRegionList() {
        //Given
        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

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
                               .category(category)
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
            assertThat(responseDto.getCategoryName()).isEqualTo(category.getName());
        }
    }

    @Test
    @DisplayName("[성공] 주소로 ListInfo DTO 페이징 목록 조회")
    void getGroupsByRegionPage() {
        //Given
        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

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
                               .category(category)
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
            assertThat(responseDto.getCategoryName()).isEqualTo(category.getName());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름과 주소로 ListInfo DTO 목록 조회")
    void getGroupsByNameContainingAndRegionList() {
        //Given
        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

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
                               .category(category)
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
            assertThat(responseDto.getCategoryName()).isEqualTo(category.getName());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름과 주소로 ListInfo DTO 페이징 목록 조회")
    void getGroupsByNameContainingAndRegionPage() {
        //Given
        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

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
                               .category(category)
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
            assertThat(responseDto.getCategoryName()).isEqualTo(category.getName());
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

        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);
        Category newCategory = Category.builder()
                                       .name("nCategory")
                                       .build();
        em.persist(newCategory);
        String newCategoryName = newCategory.getName();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .category(category)
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
                                                        .categoryName(newCategoryName)
                                                        .build();

        //When
        GroupResponse.Detail response = groupService.modifyGroup(groupId, memberId, update);

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
        assertThat(response.getCategoryName()).isNotEqualTo(category.getName());
        assertThat(response.getCategoryName()).isEqualTo(newCategoryName);
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 ID로 모임 조회 후 값 수정 시도")
    void modifyGroup_unknownId() {
        //Given
        Long unknownId = 1234567890L;

        GroupRequest.Update update = GroupRequest.Update.builder()
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

        assertThatThrownBy(() -> groupService.modifyGroup(unknownId, unknownId, update))
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

        Category category = Category.builder()
                                    .name("category")
                                    .build();
        em.persist(category);

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .category(category)
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

}
