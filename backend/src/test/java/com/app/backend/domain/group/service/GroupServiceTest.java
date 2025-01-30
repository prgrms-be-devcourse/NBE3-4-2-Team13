package com.app.backend.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.app.backend.domain.group.dto.request.GroupRequest;
import com.app.backend.domain.group.dto.response.GroupResponse;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupErrorCode;
import com.app.backend.domain.group.exception.GroupException;
import com.app.backend.domain.group.supporter.SpringBootTestSupporter;
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
    void createOrder() {
        //Given
        GroupRequest.Create request = GroupRequest.Create.builder()
                                                         .name("test")
                                                         .province("test province")
                                                         .city("test city")
                                                         .town("test town")
                                                         .description("test description")
                                                         .maxRecruitCount(10)
                                                         .build();

        //When
        Long id = groupService.createGroup(request);
        afterEach();

        //Then
        Group savedGroup = em.find(Group.class, id);

        assertThat(savedGroup.getName()).isEqualTo(request.getName());
        assertThat(savedGroup.getProvince()).isEqualTo(request.getProvince());
        assertThat(savedGroup.getCity()).isEqualTo(request.getCity());
        assertThat(savedGroup.getTown()).isEqualTo(request.getTown());
        assertThat(savedGroup.getDescription()).isEqualTo(request.getDescription());
        assertThat(savedGroup.getRecruitStatus()).isEqualTo(RecruitStatus.RECRUITING);
        assertThat(savedGroup.getMaxRecruitCount()).isEqualTo(request.getMaxRecruitCount());
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

        GroupRequest.Update update = GroupRequest.Update.builder()
                                                        .groupId(id)
                                                        .name("new test")
                                                        .province("new test province")
                                                        .city("new test city")
                                                        .town("new test town")
                                                        .description("new test description")
                                                        .recruitStatus(RecruitStatus.CLOSED.name())
                                                        .maxRecruitCount(20)
                                                        .build();

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
        GroupErrorCode errorCode = GroupErrorCode.GROUP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.modifyGroup(update))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[성공] ID로 모임 삭제(Soft Delete)")
    void deleteGroup() {
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
        boolean flag = groupService.deleteGroup(id);

        //Then
        Group deletedGroup = em.find(Group.class, id);

        assertThat(flag).isTrue();
        assertThat(deletedGroup.getDisabled()).isTrue();
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 ID로 모임 조회 후 삭제 시도")
    void deleteGroup_unknownId() {
        //Given
        Long unknownId = 1234567890L;

        //When

        //Then
        GroupErrorCode errorCode = GroupErrorCode.GROUP_NOT_FOUND;

        assertThatThrownBy(() -> groupService.deleteGroup(unknownId))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

}
