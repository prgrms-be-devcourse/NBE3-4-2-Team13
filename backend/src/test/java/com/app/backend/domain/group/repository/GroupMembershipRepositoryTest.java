package com.app.backend.domain.group.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.supporter.SpringBootTestSupporter;
import com.app.backend.domain.member.entity.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class GroupMembershipRepositoryTest extends SpringBootTestSupporter {

    @AfterEach
    void afterEach() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("[성공] GroupMembership 엔티티 저장")
    void save() {
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
                                                         .group(group)
                                                         .member(member)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        afterEach();

        //When
        groupMembershipRepository.save(groupMembership);
        afterEach();

        //Then
        GroupMembershipId groupMembershipId = GroupMembershipId.builder()
                                                               .memberId(memberId)
                                                               .groupId(groupId)
                                                               .build();

        GroupMembership findGroupMembership = em.find(GroupMembership.class, groupMembershipId);

        assertThat(findGroupMembership.getGroupId()).isEqualTo(groupId);
        assertThat(findGroupMembership.getMemberId()).isEqualTo(memberId);
        assertThat(findGroupMembership.getGroupRole()).isEqualTo(GroupRole.PARTICIPANT);
        assertThat(findGroupMembership.getStatus()).isEqualTo(MembershipStatus.PENDING);
    }

    @Test
    @DisplayName("[성공] ID로 GroupMembership 엔티티 조회")
    void findById() {
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
                                                         .group(group)
                                                         .member(member)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupMembership);
        afterEach();

        GroupMembershipId groupMembershipId = GroupMembershipId.builder()
                                                               .memberId(memberId)
                                                               .groupId(groupId)
                                                               .build();

        //When
        Optional<GroupMembership> opGroupMembership = groupMembershipRepository.findById(groupMembershipId);

        //Then
        assertThat(opGroupMembership).isPresent();
        assertThat(opGroupMembership.get().getMemberId()).isEqualTo(memberId);
        assertThat(opGroupMembership.get().getGroupId()).isEqualTo(groupId);
        assertThat(opGroupMembership.get().getGroupRole()).isEqualTo(GroupRole.PARTICIPANT);
        assertThat(opGroupMembership.get().getStatus()).isEqualTo(MembershipStatus.PENDING);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 ID로 GroupMembership 엔티티 조회 시도")
    void findById_unknownId() {
        //Given
        GroupMembershipId groupMembershipId = GroupMembershipId.builder()
                                                               .memberId(1234567890L)
                                                               .groupId(1234567890L)
                                                               .build();

        //When
        Optional<GroupMembership> opGroupMembership = groupMembershipRepository.findById(groupMembershipId);

        //Then
        assertThat(opGroupMembership).isNotPresent();
    }

    @Test
    @DisplayName("[성공] Group ID와 Member ID로 GroupMembership 엔티티 조회")
    void findByGroupIdAndMemberId() {
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
                                                         .group(group)
                                                         .member(member)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupMembership);
        afterEach();

        //When
        Optional<GroupMembership> opGroupMembership = groupMembershipRepository.findByGroupIdAndMemberId(groupId,
                                                                                                         memberId);

        //Then
        assertThat(opGroupMembership).isPresent();
        assertThat(opGroupMembership.get().getMemberId()).isEqualTo(memberId);
        assertThat(opGroupMembership.get().getGroupId()).isEqualTo(groupId);
        assertThat(opGroupMembership.get().getGroupRole()).isEqualTo(GroupRole.PARTICIPANT);
        assertThat(opGroupMembership.get().getStatus()).isEqualTo(MembershipStatus.PENDING);
    }

    @Test
    @DisplayName("[실패] Member ID와 존재하지 않는 Group ID로 GroupMembership 엔티티 조회")
    void findByGroupIdAndMemberId_unknownGroupId() {
        //Given
        Member member = Member.builder()
                              .username("testUsername")
                              .password("testPassword")
                              .nickname("testNickname")
                              .build();
        em.persist(member);
        Long memberId       = member.getId();
        Long unknownGroupId = 1234567890L;
        afterEach();

        //When
        Optional<GroupMembership> opGroupMembership = groupMembershipRepository.findByGroupIdAndMemberId(unknownGroupId,
                                                                                                         memberId);

        //Then
        assertThat(opGroupMembership).isNotPresent();
    }

    @Test
    @DisplayName("[실패] Group ID와 존재하지 않는 Member ID로 GroupMembership 엔티티 조회")
    void findByGroupIdAndMemberId_unknownMemberId() {
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
        Long groupId         = group.getId();
        Long unknownMemberId = 1234567890L;
        afterEach();

        //When
        Optional<GroupMembership> opGroupMembership = groupMembershipRepository.findByGroupIdAndMemberId(groupId,
                                                                                                         unknownMemberId);

        //Then
        assertThat(opGroupMembership).isNotPresent();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 Group ID와 Member ID로 GroupMembership 엔티티 조회")
    void findByGroupIdAndMemberId_unknownGroupIdAndMemberId() {
        //Given
        Long unknownGroupId  = 1234567890L;
        Long unknownMemberId = 1234567890L;

        //When
        Optional<GroupMembership> opGroupMembership = groupMembershipRepository.findByGroupIdAndMemberId(unknownGroupId,
                                                                                                         unknownMemberId);

        //Then
        assertThat(opGroupMembership).isNotPresent();
    }

    @Test
    @DisplayName("[성공] Group ID와 Member ID, Disabled = false로 GroupMembership 엔티티 조회")
    void findByGroupIdAndMemberIdAndDisabled() {
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
                                                         .group(group)
                                                         .member(member)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupMembership);
        afterEach();

        //When
        Optional<GroupMembership> opGroupMembership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId,
                                                                              memberId,
                                                                              false);

        //Then
        assertThat(opGroupMembership).isPresent();
        assertThat(opGroupMembership.get().getMemberId()).isEqualTo(memberId);
        assertThat(opGroupMembership.get().getGroupId()).isEqualTo(groupId);
        assertThat(opGroupMembership.get().getGroupRole()).isEqualTo(GroupRole.PARTICIPANT);
        assertThat(opGroupMembership.get().getStatus()).isEqualTo(MembershipStatus.PENDING);
    }

    @Test
    @DisplayName("[실패] Group ID와 Member ID, Disabled = true로 GroupMembership 엔티티 조회")
    void findByGroupIdAndMemberIdAndDisabled_disabled() {
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
                                                         .group(group)
                                                         .member(member)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupMembership);
        afterEach();

        //When
        Optional<GroupMembership> opGroupMembership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId,
                                                                              memberId,
                                                                              true);

        //Then
        assertThat(opGroupMembership).isNotPresent();
    }

    @Test
    @DisplayName("[실패] Member ID와 존재하지 않는 Group ID, Disabled = false로 GroupMembership 엔티티 조회")
    void findByGroupIdAndMemberIdAndDisabled_unknownGroupId() {
        //Given
        Member member = Member.builder()
                              .username("testUsername")
                              .password("testPassword")
                              .nickname("testNickname")
                              .build();
        em.persist(member);
        Long memberId       = member.getId();
        Long unknownGroupId = 1234567890L;
        afterEach();

        //When
        Optional<GroupMembership> opGroupMembership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(unknownGroupId,
                                                                              memberId,
                                                                              false);

        //Then
        assertThat(opGroupMembership).isNotPresent();
    }

    @Test
    @DisplayName("[실패] Group ID와 존재하지 않는 Member ID, Disabled = false로 GroupMembership 엔티티 조회")
    void findByGroupIdAndMemberIdAndDisabled_unknownMemberId() {
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
        Long groupId         = group.getId();
        Long unknownMemberId = 1234567890L;
        afterEach();

        //When
        Optional<GroupMembership> opGroupMembership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId,
                                                                              unknownMemberId,
                                                                              false);

        //Then
        assertThat(opGroupMembership).isNotPresent();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 Group ID와 Member ID, Disabled = false로 GroupMembership 엔티티 조회")
    void findByGroupIdAndMemberIdAndDisabled_unknownGroupIdAndMemberId() {
        //Given
        Long unknownGroupId  = 1234567890L;
        Long unknownMemberId = 1234567890L;

        //When
        Optional<GroupMembership> opGroupMembership =
                groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(unknownGroupId,
                                                                              unknownMemberId,
                                                                              false);

        //Then
        assertThat(opGroupMembership).isNotPresent();
    }

    @Test
    @DisplayName("[성공] Group ID로 GroupMembership 엔티티 목록 조회")
    void findAllByGroupId() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        List<GroupMembership> findGroupMemberships = groupMembershipRepository.findAllByGroupId(groupId);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getGroupId().equals(groupId))
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[성공] Group ID와 Disabled = false로 GroupMembership 엔티티 목록 조회")
    void findAllByGroupIdAndDisabled() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        List<GroupMembership> findGroupMemberships = groupMembershipRepository.findAllByGroupIdAndDisabled(groupId,
                                                                                                           false);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getGroupId().equals(groupId)
                                                                      && !groupMembership.getDisabled())
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[실패] Group ID와 Disabled = true로 GroupMembership 엔티티 목록 조회")
    void findAllByGroupIdAndDisabled_disabled() {
        //Given
        int   size  = 20;
        int   j     = 0;
        Group group = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        List<GroupMembership> findGroupMemberships = groupMembershipRepository.findAllByGroupIdAndDisabled(groupId,
                                                                                                           false);

        //Then
        assertThat(findGroupMemberships).isEmpty();
    }

    @Test
    @DisplayName("[성공] Member ID로 GroupMembership 엔티티 목록 조회")
    void findAllByMemberId() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long memberId = 3L;

        //When
        List<GroupMembership> findGroupMemberships = groupMembershipRepository.findAllByMemberId(memberId);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getMemberId().equals(memberId))
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[성공] Member ID와 Disabled = false로 GroupMembership 엔티티 목록 조회")
    void findAllByMemberIdAndDisabled() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long memberId = 3L;

        //When
        List<GroupMembership> findGroupMemberships = groupMembershipRepository.findAllByMemberIdAndDisabled(memberId,
                                                                                                            false);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getMemberId().equals(memberId)
                                                                      && !groupMembership.getDisabled())
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[실패] Member ID와 Disabled = true로 GroupMembership 엔티티 목록 조회")
    void findAllByMemberIdAndDisabled_disabled() {
        //Given
        int   size  = 20;
        int   j     = 0;
        Group group = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
        }
        afterEach();

        Long memberId = 3L;

        //When
        List<GroupMembership> findGroupMemberships = groupMembershipRepository.findAllByMemberIdAndDisabled(memberId,
                                                                                                            false);

        //Then
        assertThat(findGroupMemberships).isEmpty();
    }

    @Test
    @DisplayName("[성공] 모임 권한으로 GroupMembership 엔티티 목록 조회")
    void findAllByGroupRole() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        //When
        List<GroupMembership> findGroupMemberships =
                groupMembershipRepository.findAllByGroupRole(GroupRole.PARTICIPANT);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getGroupRole()
                                                                                     .equals(GroupRole.PARTICIPANT))
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[성공] 모임 권한과 Disabled = false로 GroupMembership 엔티티 목록 조회")
    void findAllByGroupRoleAndDisabled() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        //When
        List<GroupMembership> findGroupMemberships =
                groupMembershipRepository.findAllByGroupRoleAndDisabled(GroupRole.PARTICIPANT, false);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getGroupRole()
                                                                                     .equals(GroupRole.PARTICIPANT)
                                                                      && !groupMembership.getDisabled())
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[실패] 모임 권한과 Disabled = true로 GroupMembership 엔티티 목록 조회")
    void findAllByGroupRoleAndDisabled_disabled() {
        //Given
        int   size  = 20;
        int   j     = 0;
        Group group = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
        }
        afterEach();

        //When
        List<GroupMembership> findGroupMemberships =
                groupMembershipRepository.findAllByGroupRoleAndDisabled(GroupRole.PARTICIPANT, true);

        //Then
        assertThat(findGroupMemberships).isEmpty();
    }

    @Test
    @DisplayName("[성공] Group ID와 모임 권한으로 GroupMembership 엔티티 목록 조회")
    void findAllByGroupIdAndGroupRole() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        List<GroupMembership> findGroupMemberships = groupMembershipRepository.findAllByGroupIdAndGroupRole(groupId,
                                                                                                            GroupRole.PARTICIPANT);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getGroupId().equals(groupId)
                                                                      && groupMembership.getGroupRole()
                                                                                        .equals(GroupRole.PARTICIPANT))
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[성공] Group ID와 모임 권한, Disabled = false로 GroupMembership 엔티티 목록 조회")
    void findAllByGroupIdAndGroupRoleAndDisabled() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        List<GroupMembership> findGroupMemberships =
                groupMembershipRepository.findAllByGroupIdAndGroupRoleAndDisabled(groupId,
                                                                                  GroupRole.PARTICIPANT,
                                                                                  false);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getGroupId().equals(groupId)
                                                                      && groupMembership.getGroupRole()
                                                                                        .equals(GroupRole.PARTICIPANT)
                                                                      && !groupMembership.getDisabled())
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[실패] Group ID와 모임 권한, Disabled = true로 GroupMembership 엔티티 목록 조회")
    void findAllByGroupIdAndGroupRoleAndDisabled_disabled() {
        //Given
        int   size  = 20;
        int   j     = 0;
        Group group = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        List<GroupMembership> findGroupMemberships =
                groupMembershipRepository.findAllByGroupIdAndGroupRoleAndDisabled(groupId,
                                                                                  GroupRole.PARTICIPANT,
                                                                                  true);

        //Then
        assertThat(findGroupMemberships).isEmpty();
    }

    @Test
    @DisplayName("[성공] Member ID와 모임 권한으로 GroupMembership 엔티티 목록 조회")
    void findAllByMemberIdAndGroupRole() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long memberId = 3L;

        //When
        List<GroupMembership> findGroupMemberships = groupMembershipRepository.findAllByMemberIdAndGroupRole(memberId,
                                                                                                             GroupRole.PARTICIPANT);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getMemberId().equals(memberId)
                                                                      && groupMembership.getGroupRole()
                                                                                        .equals(GroupRole.PARTICIPANT))
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[성공] Member ID와 모임 권한, Disabled = false로 GroupMembership 엔티티 목록 조회")
    void findAllByMemberIdAndGroupRoleAndDisabled() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long memberId = 3L;

        //When
        List<GroupMembership> findGroupMemberships =
                groupMembershipRepository.findAllByMemberIdAndGroupRoleAndDisabled(memberId,
                                                                                   GroupRole.PARTICIPANT,
                                                                                   false);

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getMemberId().equals(memberId)
                                                                      && groupMembership.getGroupRole()
                                                                                        .equals(GroupRole.PARTICIPANT)
                                                                      && !groupMembership.getDisabled())
                                           .toList();

        assertThat(findGroupMemberships).hasSize(groupMemberships.size());
        for (int i = 0; i < groupMemberships.size(); i++) {
            GroupMembership groupMembership     = groupMemberships.get(i);
            GroupMembership findGroupMembership = findGroupMemberships.get(i);

            assertThat(findGroupMembership.getGroupId()).isEqualTo(groupMembership.getGroupId());
            assertThat(findGroupMembership.getMemberId()).isEqualTo(groupMembership.getMemberId());
            assertThat(findGroupMembership.getGroupRole()).isEqualTo(groupMembership.getGroupRole());
            assertThat(findGroupMembership.getStatus()).isEqualTo(groupMembership.getStatus());
        }
    }

    @Test
    @DisplayName("[실패] Member ID와 모임 권한, Disabled = true로 GroupMembership 엔티티 목록 조회")
    void findAllByMemberIdAndGroupRoleAndDisabled_disabled() {
        //Given
        int   size  = 20;
        int   j     = 0;
        Group group = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
        }
        afterEach();

        Long memberId = 3L;

        //When
        List<GroupMembership> findGroupMemberships =
                groupMembershipRepository.findAllByMemberIdAndGroupRoleAndDisabled(memberId,
                                                                                   GroupRole.PARTICIPANT,
                                                                                   true);

        //Then
        assertThat(findGroupMemberships).isEmpty();
    }

    @Test
    @DisplayName("[성공] Group ID와 Member ID로 GroupMembership 엔티티 존재 여부 확인")
    void existsByGroupIdAndMemberId() {
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
                                                         .group(group)
                                                         .member(member)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupMembership);
        afterEach();

        //When
        boolean flag = groupMembershipRepository.existsByGroupIdAndMemberId(groupId, memberId);

        //Then
        assertThat(flag).isTrue();
    }

    @Test
    @DisplayName("[성공] Group ID와 Member ID, Disabled로 GroupMembership 엔티티 존재 여부 확인")
    void existsByGroupIdAndMemberIdAndDisabled() {
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
                                                         .group(group)
                                                         .member(member)
                                                         .groupRole(GroupRole.PARTICIPANT)
                                                         .build();
        em.persist(groupMembership);
        afterEach();

        //When
        boolean flag = groupMembershipRepository.existsByGroupIdAndMemberIdAndDisabled(groupId, memberId, false);

        //Then
        assertThat(flag).isTrue();
    }

    @Test
    @DisplayName("[성공] Group ID와 모임 권한(단수)으로 GroupMembership 엔티티 수 조회")
    void countByGroupIdAndGroupRole() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        int count = groupMembershipRepository.countByGroupIdAndGroupRole(groupId, GroupRole.PARTICIPANT);

        //Then
        int filteredCount = Math.toIntExact(
                groupMemberships.stream().filter(groupMembership -> groupMembership.getGroupId().equals(groupId)
                                                                    && groupMembership.getGroupRole()
                                                                                      .equals(GroupRole.PARTICIPANT))
                                .count()
        );

        assertThat(count).isEqualTo(filteredCount);
    }

    @Test
    @DisplayName("[성공] Group ID와 모임 권한(단수), Disabled = false로 GroupMembership 엔티티 수 조회")
    void countByGroupIdAndGroupRoleAndDisabled() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        int count = groupMembershipRepository.countByGroupIdAndGroupRoleAndDisabled(groupId,
                                                                                    GroupRole.PARTICIPANT,
                                                                                    false);

        //Then
        int filteredCount = Math.toIntExact(
                groupMemberships.stream()
                                .filter(groupMembership -> groupMembership.getGroupId().equals(groupId)
                                                           && groupMembership.getGroupRole()
                                                                             .equals(GroupRole.PARTICIPANT)
                                                           && !groupMembership.getDisabled())
                                .count()
        );

        assertThat(count).isEqualTo(filteredCount);
    }

    @Test
    @DisplayName("[성공] Group ID와 모임 권한(복수)으로 GroupMembership 엔티티 수 조회")
    void countByGroupIdAndGroupRoleIn() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;
        GroupMembership       groupMembership  = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;

                groupMembership = GroupMembership.builder()
                                                 .group(group)
                                                 .member(member)
                                                 .groupRole(GroupRole.LEADER)
                                                 .build();
                em.persist(groupMembership);
            } else {
                groupMembership = GroupMembership.builder()
                                                 .group(group)
                                                 .member(member)
                                                 .groupRole(GroupRole.PARTICIPANT)
                                                 .build();
                em.persist(groupMembership);
            }

            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long           groupId    = 3L;
        Set<GroupRole> groupRoles = Set.of(GroupRole.LEADER, GroupRole.PARTICIPANT);

        //When
        int count = groupMembershipRepository.countByGroupIdAndGroupRoleIn(groupId, groupRoles);

        //Then
        int filteredCount = Math.toIntExact(
                groupMemberships.stream()
                                .filter(g -> g.getGroupId().equals(groupId) && groupRoles.contains(g.getGroupRole()))
                                .count()
        );

        assertThat(count).isEqualTo(filteredCount);
    }

    @Test
    @DisplayName("[성공] Group ID와 모임 권한(복수), Disabled = false로 GroupMembership 엔티티 수 조회")
    void countByGroupIdAndGroupRoleInAndDisabled() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;
        GroupMembership       groupMembership  = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;

                groupMembership = GroupMembership.builder()
                                                 .group(group)
                                                 .member(member)
                                                 .groupRole(GroupRole.LEADER)
                                                 .build();
                em.persist(groupMembership);
            } else {
                groupMembership = GroupMembership.builder()
                                                 .group(group)
                                                 .member(member)
                                                 .groupRole(GroupRole.PARTICIPANT)
                                                 .build();
                em.persist(groupMembership);
            }

            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long           groupId    = 3L;
        Set<GroupRole> groupRoles = Set.of(GroupRole.LEADER, GroupRole.PARTICIPANT);

        //When
        int count = groupMembershipRepository.countByGroupIdAndGroupRoleInAndDisabled(groupId, groupRoles, false);

        //Then
        int filteredCount = Math.toIntExact(
                groupMemberships.stream()
                                .filter(g -> g.getGroupId().equals(groupId)
                                             && groupRoles.contains(g.getGroupRole())
                                             && !g.getDisabled())
                                .count()
        );

        assertThat(count).isEqualTo(filteredCount);
    }

    @Test
    @DisplayName("[성공] Group ID와 모임 내 회원 상태로 GroupMembership 엔티티 수 조회")
    void countByGroupIdAndStatus() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;
        GroupMembership       groupMembership  = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;

                groupMembership = GroupMembership.builder()
                                                 .group(group)
                                                 .member(member)
                                                 .groupRole(GroupRole.LEADER)
                                                 .build();
                em.persist(groupMembership);
            } else {
                groupMembership = GroupMembership.builder()
                                                 .group(group)
                                                 .member(member)
                                                 .groupRole(GroupRole.PARTICIPANT)
                                                 .build();
                em.persist(groupMembership);
            }

            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        int count = groupMembershipRepository.countByGroupIdAndStatus(groupId, MembershipStatus.PENDING);

        //Then
        int filteredCount = Math.toIntExact(
                groupMemberships.stream()
                                .filter(g -> g.getGroupId().equals(groupId)
                                             && g.getStatus().equals(MembershipStatus.PENDING))
                                .count()
        );

        assertThat(count).isEqualTo(filteredCount);
    }

    @Test
    @DisplayName("[성공] Group ID와 모임 내 회원 상태, Disabled = false로 GroupMembership 엔티티 수 조회")
    void countByGroupIdAndStatusAndDisabled() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;
        GroupMembership       groupMembership  = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;

                groupMembership = GroupMembership.builder()
                                                 .group(group)
                                                 .member(member)
                                                 .groupRole(GroupRole.LEADER)
                                                 .build();
                em.persist(groupMembership);
            } else {
                groupMembership = GroupMembership.builder()
                                                 .group(group)
                                                 .member(member)
                                                 .groupRole(GroupRole.PARTICIPANT)
                                                 .build();
                em.persist(groupMembership);
            }

            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        int count = groupMembershipRepository.countByGroupIdAndStatusAndDisabled(groupId,
                                                                                 MembershipStatus.PENDING,
                                                                                 false);

        //Then
        int filteredCount = Math.toIntExact(
                groupMemberships.stream()
                                .filter(g -> g.getGroupId().equals(groupId)
                                             && g.getStatus().equals(MembershipStatus.PENDING)
                                             && !g.getDisabled())
                                .count()
        );

        assertThat(count).isEqualTo(filteredCount);
    }

    @Test
    @DisplayName("[성공] Group ID로 해당 모임의 모든 멤버십 활성화 상태를 일괄 Disabled = true로 변경")
    void updateDisabledForAllGroupMembership() {
        //Given
        int                   size             = 20;
        List<GroupMembership> groupMemberships = new ArrayList<>();
        int                   j                = 0;
        Group                 group            = null;

        for (int i = 0; i < size; i++) {
            Member member = Member.builder()
                                  .username("testUsername%d".formatted(i))
                                  .password("testPassword%d".formatted(i))
                                  .nickname("testNickname%d".formatted(i))
                                  .build();
            em.persist(member);

            if (j % 5 == 0) {
                group = Group.builder()
                             .name("test%d".formatted(j))
                             .province("test province%d".formatted(j))
                             .city("test city%d".formatted(j))
                             .town("test town%d".formatted(j))
                             .description("test description%d".formatted(j))
                             .recruitStatus(RecruitStatus.RECRUITING)
                             .maxRecruitCount(10)
                             .build();
                em.persist(group);
                j += 1;
            }

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .group(group)
                                                             .member(member)
                                                             .groupRole(GroupRole.PARTICIPANT)
                                                             .build();
            em.persist(groupMembership);
            groupMemberships.add(groupMembership);
        }
        afterEach();

        Long groupId = 3L;

        //When
        int updatedCount = groupMembershipRepository.updateDisabledForAllGroupMembership(groupId, true);
        afterEach();

        //Then
        groupMemberships = groupMemberships.stream()
                                           .filter(groupMembership -> groupMembership.getGroupId().equals(groupId)
                                                                      && !groupMembership.getDisabled())
                                           .toList();
        List<GroupMembership> updatedGroupMemberships = groupMembershipRepository.findAllByGroupId(groupId);

        assertThat(updatedCount).isEqualTo(groupMemberships.size());
        assertThat(updatedGroupMemberships.stream().anyMatch(g -> !g.getDisabled())).isFalse();
    }

}