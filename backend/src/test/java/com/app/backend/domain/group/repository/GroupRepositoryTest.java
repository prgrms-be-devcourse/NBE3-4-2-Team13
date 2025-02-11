package com.app.backend.domain.group.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.supporter.SpringBootTestSupporter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class GroupRepositoryTest extends SpringBootTestSupporter {

    private Category category;

    @BeforeEach
    void beforeEach() {
        category = Category.builder()
                           .name("category")
                           .build();
        em.persist(category);
    }

    @AfterEach
    void afterEach() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("[성공] Group 엔티티 저장")
    void save() {
        //Given
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

        //When
        Long id = groupRepository.save(group).getId();
        afterEach();

        //Then
        Group findGroup = em.find(Group.class, id);

        assertThat(findGroup.getId()).isEqualTo(id);
        assertThat(findGroup.getName()).isEqualTo(group.getName());
        assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
        assertThat(findGroup.getCity()).isEqualTo(group.getCity());
        assertThat(findGroup.getTown()).isEqualTo(group.getTown());
        assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
        assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
        assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
    }

    @Test
    @DisplayName("[성공] ID로 Group 엔티티 조회")
    void findById() {
        //Given
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
        Optional<Group> opGroup = groupRepository.findById(id);

        //Then
        assertThat(opGroup).isPresent();
        assertThat(opGroup.get().getId()).isEqualTo(id);
        assertThat(opGroup.get().getName()).isEqualTo(group.getName());
        assertThat(opGroup.get().getProvince()).isEqualTo(group.getProvince());
        assertThat(opGroup.get().getCity()).isEqualTo(group.getCity());
        assertThat(opGroup.get().getTown()).isEqualTo(group.getTown());
        assertThat(opGroup.get().getDescription()).isEqualTo(group.getDescription());
        assertThat(opGroup.get().getRecruitStatus()).isEqualTo(group.getRecruitStatus());
        assertThat(opGroup.get().getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 ID로 Group 엔티티 조회 시도")
    void findById_unknownId() {
        //Given
        Long unknownId = 1234567890L;

        //When
        Optional<Group> opGroup = groupRepository.findById(unknownId);

        //Then
        assertThat(opGroup).isNotPresent();
    }

    @Test
    @DisplayName("[성공] ID와 Disabled = false로 Group 엔티티 조회")
    void findByIdAndDisabled() {
        //Given
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
        Optional<Group> opGroup = groupRepository.findByIdAndDisabled(id, false);

        //Then
        assertThat(opGroup).isPresent();
        assertThat(opGroup.get().getId()).isEqualTo(id);
        assertThat(opGroup.get().getName()).isEqualTo(group.getName());
        assertThat(opGroup.get().getProvince()).isEqualTo(group.getProvince());
        assertThat(opGroup.get().getCity()).isEqualTo(group.getCity());
        assertThat(opGroup.get().getTown()).isEqualTo(group.getTown());
        assertThat(opGroup.get().getDescription()).isEqualTo(group.getDescription());
        assertThat(opGroup.get().getRecruitStatus()).isEqualTo(group.getRecruitStatus());
        assertThat(opGroup.get().getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
    }

    @Test
    @DisplayName("[실패] ID와 Disabled = true로 Group 엔티티 조회")
    void findByIdAndDisabled_disabled() {
        //Given
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
        Optional<Group> opGroup = groupRepository.findByIdAndDisabled(id, true);

        //Then
        assertThat(opGroup).isNotPresent();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 ID와 Disabled로 Group 엔티티 조회 시도")
    void findByIdAndDisabled_unknownId() {
        //Given
        Long unknownId = 1234567890L;

        //When
        Optional<Group> opGroup = groupRepository.findByIdAndDisabled(unknownId, false);

        //Then
        assertThat(opGroup).isNotPresent();
    }

    @Test
    @DisplayName("[성공] Diabled = false로 Group 엔티티 목록 조회")
    void findAllListByDisabled() {
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
                               .category(category)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        //When
        List<Group> findGroups = groupRepository.findAllByDisabled(false);

        //Then
        assertThat(findGroups).hasSize(size);
        for (int i = 0; i < size; i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[실패] Diabled = true로 Group 엔티티 목록 조회")
    void findAllListByDisabled_disabled() {
        //Given
        int size = 20;
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
            em.persist(group);
        }
        afterEach();

        //When
        List<Group> findGroups = groupRepository.findAllByDisabled(true);

        //Then
        assertThat(findGroups).isEmpty();
    }

    @Test
    @DisplayName("[성공] Diabled = false로 Group 엔티티 페이징 목록 조회")
    void findAllPageByDisabled() {
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
                               .category(category)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        //When
        Page<Group> findGroupPage = groupRepository.findAllByDisabled(false, pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();
        groups = groups.subList(0, pageable.getPageSize());

        assertThat(findGroups).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < findGroups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[실패] Diabled = true로 Group 엔티티 페이징 목록 조회")
    void findAllPageByDisabled_disabled() {
        //Given
        int size = 20;
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
            em.persist(group);
        }
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        //When
        Page<Group> findGroupPage = groupRepository.findAllByDisabled(true, pageable);

        //Then
        assertThat(findGroupPage).isEmpty();
    }

    @Test
    @DisplayName("[성공] 모임 이름과 Diabled = false로 Group 엔티티 목록 조회")
    void findAllListByNameContainingAndDisabled() {
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
                               .category(category)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        String name = "5";

        //When
        List<Group> findGroups = groupRepository.findAllByNameContainingAndDisabled(name, false);

        //Then
        groups = groups.stream().filter(group -> group.getName().contains(name)).toList();

        assertThat(findGroups).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[실패] 알 수 없는 모임 이름과 Disabled = false로 Gruop 엔티티 목록 조회")
    void findAllListByNameContainingAndDisabled_unknownName() {
        //Given
        int size = 20;
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
            em.persist(group);
        }
        afterEach();

        String name = "unknown";

        //When
        List<Group> findGroups = groupRepository.findAllByNameContainingAndDisabled(name, false);

        //Then
        assertThat(findGroups).isEmpty();
    }

    @Test
    @DisplayName("[실패] 모임 이름과 Disabled = true로 Gruop 엔티티 목록 조회")
    void findAllListByNameContainingAndDisabled_disabled() {
        //Given
        int size = 20;
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
            em.persist(group);
        }
        afterEach();

        String name = "5";

        //When
        List<Group> findGroups = groupRepository.findAllByNameContainingAndDisabled(name, true);

        //Then
        assertThat(findGroups).isEmpty();
    }

    @Test
    @DisplayName("[성공] 모임 이름과 Diabled로 Group 엔티티 페이징 목록 조회")
    void findAllPageByNameContainingAndDisabled() {
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
                               .category(category)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        String name = "5";

        //When
        Page<Group> findGroupPage = groupRepository.findAllByNameContainingAndDisabled(name, false, pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();
        groups = groups.stream().filter(group -> group.getName().contains(name)).limit(pageable.getPageSize()).toList();

        assertThat(findGroups).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < findGroups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[실패] 알 수 없는 모임 이름과 Disabled = false로 Gruop 엔티티 페이징 목록 조회")
    void findAllPageByNameContainingAndDisabled_unknownName() {
        //Given
        int size = 20;
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
            em.persist(group);
        }
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        String name = "unknown";

        //When
        Page<Group> findGroupPage = groupRepository.findAllByNameContainingAndDisabled(name, false, pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();

        assertThat(findGroups).isEmpty();
    }

    @Test
    @DisplayName("[실패] 모임 이름과 Disabled = true Gruop 엔티티 페이징 목록 조회")
    void findAllPageByNameContainingAndDisabled_disabled() {
        //Given
        int size = 20;
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
            em.persist(group);
        }
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        String name = "5";

        //When
        Page<Group> findGroupPage = groupRepository.findAllByNameContainingAndDisabled(name, true, pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();

        assertThat(findGroups).isEmpty();
    }

    @Test
    @DisplayName("[성공] 상세 주소와 Disabled로 Group 엔티티 목록 조회")
    void findAllListByRegionAndDisabled() {
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
        List<Group> findGroups = groupRepository.findAllByRegion(province, city, town, false);

        //Then
        groups = groups.stream().filter(group -> group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(findGroups).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 상세 주소와 Disabled로 Group 엔티티 페이징 목록 조회")
    void findAllPageByRegionAndDisabled() {
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
                               .category(category)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        String province = "test province10";
        String city     = "test city10";
        String town     = "test town10";

        //When
        Page<Group> findGroupPage = groupRepository.findAllByRegion(province, city, town, false, pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();
        groups = groups.stream().filter(group -> group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(findGroups).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < findGroups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름, 상세 주소와 Disabled로 Group 엔티티 목록 조회")
    void findAllListByNameContainingAndRegionAndDisabled() {
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
                               .category(category)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        afterEach();

        String name     = "1";
        String province = "test province10";
        String city     = "test city10";
        String town     = "test town10";

        //When
        List<Group> findGroups = groupRepository.findAllByNameContainingAndRegion(name, province, city, town, false);

        //Then
        groups = groups.stream().filter(group -> group.getName().contains(name)
                                                 && group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(findGroups).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 모임 이름, 상세 주소와 Disabled로 Group 엔티티 페이징 목록 조회")
    void findAllPageByNameContainingAndRegionAndDisabled() {
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
                               .category(category)
                               .build();
            groups.add(group);
            em.persist(group);
        }
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        String name     = "1";
        String province = "test province10";
        String city     = "test city10";
        String town     = "test town10";

        //When
        Page<Group> findGroupPage =
                groupRepository.findAllByNameContainingAndRegion(name, province, city, town, false, pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();
        groups = groups.stream().filter(group -> group.getName().contains(name)
                                                 && group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(findGroups).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < findGroups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명으로 Group 엔티티 목록 조회")
    void findAllListByCategory_Name() {
        //Given
        String categoryName = category.getName();

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
        List<Group> findGroups = groupRepository.findAllByCategory_Name(categoryName);

        //Then
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)).toList();

        assertThat(findGroups).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명으로 Group 엔티티 페이징 목록 조회")
    void findAllPageByCategory_Name() {
        //Given
        String categoryName = category.getName();

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
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        //When
        Page<Group> findGroupPage = groupRepository.findAllByCategory_Name(categoryName, pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)).toList();

        assertThat(findGroups).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < findGroups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명과 Disabled로 Group 엔티티 목록 조회")
    void findAllListByCategory_NameAndDisabled() {
        //Given
        String categoryName = category.getName();

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
        List<Group> findGroups = groupRepository.findAllByCategory_NameAndDisabled(categoryName, false);

        //Then
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)
                                                 && !group.getDisabled()).toList();

        assertThat(findGroups).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명과 Disabled로 Group 엔티티 페이징 목록 조회")
    void findAllPageByCategory_NameAndDisabled() {
        //Given
        String categoryName = category.getName();

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
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        //When
        Page<Group> findGroupPage = groupRepository.findAllByCategory_NameAndDisabled(categoryName,
                                                                                      false,
                                                                                      pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)
                                                 && !group.getDisabled()).toList();

        assertThat(findGroups).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < findGroups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명, 모임 이름과 Disabled로 Group 엔티티 목록 조회")
    void findAllListByCategory_NameAndNameContainingAndDisabled() {
        //Given
        String categoryName = category.getName();

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
        List<Group> findGroups = groupRepository.findAllByCategory_NameAndNameContainingAndDisabled(categoryName,
                                                                                                    name,
                                                                                                    false);

        //Then
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)
                                                 && group.getName().contains(name)
                                                 && !group.getDisabled()).toList();

        assertThat(findGroups).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명, 모임 이름과 Disabled로 Group 엔티티 페이징 목록 조회")
    void findAllPageByCategory_NameAndNameContainingAndDisabled() {
        //Given
        String categoryName = category.getName();

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
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        String name = "5";

        //When
        Page<Group> findGroupPage = groupRepository.findAllByCategory_NameAndNameContainingAndDisabled(categoryName,
                                                                                                       name,
                                                                                                       false,
                                                                                                       pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)
                                                 && group.getName().contains(name)
                                                 && !group.getDisabled()).toList();

        assertThat(findGroups).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < findGroups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명, 모임 이름과 상세 주소, Disabled로 Group 엔티티 목록 조회")
    void findAllListByCategoryAndNameContainingAndRegion() {
        //Given
        String categoryName = category.getName();

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

        String name     = "1";
        String province = "test province10";
        String city     = "test city10";
        String town     = "test town10";

        //When
        List<Group> findGroups = groupRepository.findAllByCategoryAndNameContainingAndRegion(categoryName,
                                                                                             name,
                                                                                             province,
                                                                                             city,
                                                                                             town,
                                                                                             false);

        //Then
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)
                                                 && group.getName().contains(name)
                                                 && group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(findGroups).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명, 모임 이름과 상세 주소, Disabled로 Group 엔티티 페이징 목록 조회")
    void findAllPageByCategoryAndNameContainingAndRegion() {
        //Given
        String categoryName = category.getName();

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
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        String name     = "1";
        String province = "test province10";
        String city     = "test city10";
        String town     = "test town10";

        //When
        Page<Group> findGroupPage = groupRepository.findAllByCategoryAndNameContainingAndRegion(categoryName,
                                                                                                name,
                                                                                                province,
                                                                                                city,
                                                                                                town,
                                                                                                false,
                                                                                                pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)
                                                 && group.getName().contains(name)
                                                 && group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(findGroups).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < findGroups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명, 모집 상태, 모임 이름과 상세 주소, Disabled로 Group 엔티티 목록 조회")
    void findAllListByCategoryAndRecruitStatusAndNameContainingAndRegion() {
        //Given
        String categoryName = category.getName();

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

        String recruitStatus = "RECRUITING";
        String name          = "1";
        String province      = "test province10";
        String city          = "test city10";
        String town          = "test town10";

        //When
        List<Group> findGroups =
                groupRepository.findAllByCategoryAndRecruitStatusAndNameContainingAndRegion(categoryName,
                                                                                            recruitStatus,
                                                                                            name,
                                                                                            province,
                                                                                            city,
                                                                                            town,
                                                                                            false);

        //Then
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)
                                                 && group.getName().contains(name)
                                                 && group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(findGroups).hasSize(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] 카테고리명, 모집 상태, 모임 이름과 상세 주소, Disabled로 Group 엔티티 페이징 목록 조회")
    void findAllPageByCategoryAndRecruitStatusAndNameContainingAndRegion() {
        //Given
        String categoryName = category.getName();

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
        Pageable pageable = PageRequest.of(0, 10);
        afterEach();

        String recruitStatus = "RECRUITING";
        String name          = "1";
        String province      = "test province10";
        String city          = "test city10";
        String town          = "test town10";

        //When
        Page<Group> findGroupPage =
                groupRepository.findAllByCategoryAndRecruitStatusAndNameContainingAndRegion(categoryName,
                                                                                            recruitStatus,
                                                                                            name,
                                                                                            province,
                                                                                            city,
                                                                                            town,
                                                                                            false,
                                                                                            pageable);

        //Then
        List<Group> findGroups = findGroupPage.getContent();
        groups = groups.stream().filter(group -> group.getCategory().getName().equals(categoryName)
                                                 && group.getName().contains(name)
                                                 && group.getProvince().equals(province)
                                                 && group.getCity().equals(city)
                                                 && group.getTown().equals(town)).toList();

        assertThat(findGroups).hasSizeLessThanOrEqualTo(pageable.getPageSize());
        for (int i = 0; i < findGroups.size(); i++) {
            Group group     = groups.get(i);
            Group findGroup = findGroups.get(i);

            assertThat(findGroup.getName()).isEqualTo(group.getName());
            assertThat(findGroup.getProvince()).isEqualTo(group.getProvince());
            assertThat(findGroup.getCity()).isEqualTo(group.getCity());
            assertThat(findGroup.getTown()).isEqualTo(group.getTown());
            assertThat(findGroup.getDescription()).isEqualTo(group.getDescription());
            assertThat(findGroup.getRecruitStatus()).isEqualTo(group.getRecruitStatus());
            assertThat(findGroup.getMaxRecruitCount()).isEqualTo(group.getMaxRecruitCount());
        }
    }

    @Test
    @DisplayName("[성공] ID로 Group 엔티티 조회 후 값 수정")
    void update() {
        //Given
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
        groupRepository.findById(id).ifPresent(g -> {
            g.modifyName("new test");
            g.modifyRegion("new test province", "new test city", "new test town");
            g.modifyDescription("new test description");
            g.modifyRecruitStatus(RecruitStatus.CLOSED);
            g.modifyMaxRecruitCount(20);
        });
        afterEach();

        //Then
        Group findGroup = em.find(Group.class, id);

        assertThat(findGroup.getId()).isEqualTo(id);
        assertThat(findGroup.getName()).isNotEqualTo(group.getName());
        assertThat(findGroup.getName()).isEqualTo("new test");
        assertThat(findGroup.getProvince()).isNotEqualTo(group.getProvince());
        assertThat(findGroup.getProvince()).isEqualTo("new test province");
        assertThat(findGroup.getCity()).isNotEqualTo(group.getCity());
        assertThat(findGroup.getCity()).isEqualTo("new test city");
        assertThat(findGroup.getTown()).isNotEqualTo(group.getTown());
        assertThat(findGroup.getTown()).isEqualTo("new test town");
        assertThat(findGroup.getDescription()).isNotEqualTo(group.getDescription());
        assertThat(findGroup.getDescription()).isEqualTo("new test description");
        assertThat(findGroup.getRecruitStatus()).isNotEqualTo(group.getRecruitStatus());
        assertThat(findGroup.getRecruitStatus()).isEqualTo(RecruitStatus.CLOSED);
        assertThat(findGroup.getMaxRecruitCount()).isNotEqualTo(group.getMaxRecruitCount());
        assertThat(findGroup.getMaxRecruitCount()).isEqualTo(20);
    }

    @Test
    @DisplayName("[성공] ID로 Group 엔티티 Hard Delete")
    void deleteById() {
        //Given
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
        groupRepository.deleteById(id);

        //Then
        Group deletedGroup = em.find(Group.class, id);

        assertThat(deletedGroup).isNull();
    }

    @Test
    @DisplayName("[성공] ID로 Group 엔티티 조회 후 Soft Delete")
    void softDelete() {
        //Given
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
        groupRepository.findById(id).ifPresent(Group::delete);
        afterEach();

        //Then
        Group findGroup = em.find(Group.class, id);

        assertThat(findGroup.getDisabled()).isTrue();
    }

}