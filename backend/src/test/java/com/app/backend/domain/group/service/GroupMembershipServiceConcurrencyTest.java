package com.app.backend.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.supporter.SpringBootTestSupporter;
import com.app.backend.domain.member.entity.Member;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Tag("concurrency")
@Slf4j
@SqlGroup({
        @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
             scripts = "classpath:/sql/truncate_tbl.sql"),
        @Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
             scripts = "classpath:/sql/truncate_tbl.sql")
})
class GroupMembershipServiceConcurrencyTest extends SpringBootTestSupporter {

    private static final int THREAD_COUNT = 100;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("[Normal] approveJoining(): 여러 클라이언트에서 동시에 같은 Group ID와 Member ID로 모임 신청 허가/거부 시도")
    void approveJoining() throws Exception {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);

        List<AtomicReference<Member>> leaderRefs = List.of(new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>());
        AtomicReference<Member> memberRef = new AtomicReference<>();
        AtomicReference<Group>  groupRef  = new AtomicReference<>();

        Future<?> future = executorService.submit(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Category category = Category.builder().name("category").build();
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
                groupRef.set(group);

                for (int i = 1; i <= leaderRefs.size(); i++) {
                    Member member = Member.builder()
                                          .username("testLeaderUsername%d".formatted(i))
                                          .password("testLeaderPassword%d".formatted(i))
                                          .nickname("testLeaderNickname%d".formatted(i))
                                          .build();
                    em.persist(member);
                    GroupMembership groupMembership = GroupMembership.builder()
                                                                     .member(member)
                                                                     .group(group)
                                                                     .groupRole(GroupRole.LEADER)
                                                                     .build();
                    em.persist(groupMembership);
                    leaderRefs.get(i - 1).set(member);
                }
                Member member = Member.builder()
                                      .username("testUsername")
                                      .password("testPassword")
                                      .nickname("testNickname")
                                      .build();
                em.persist(member);
                memberRef.set(member);
                GroupMembership groupMembership = GroupMembership.builder()
                                                                 .member(member)
                                                                 .group(group)
                                                                 .groupRole(GroupRole.PARTICIPANT)
                                                                 .build();
                em.persist(groupMembership);

                transactionManager.commit(transactionStatus);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            }
        });
        future.get();

        List<Member> leaders = leaderRefs.stream().map(AtomicReference::get).toList();
        Group        group   = groupRef.get();
        Member       member  = memberRef.get();

        Long       groupId   = group.getId();
        List<Long> leaderIds = leaders.stream().map(Member::getId).toList();
        Long       memberId  = member.getId();

        //When
        Set<Integer>  methodCallSuccessThreads = ConcurrentHashMap.newKeySet();
        AtomicInteger firstUpdatedThreadIndex  = new AtomicInteger();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    Thread.sleep(10);
                    boolean flag = groupMembershipService.approveJoining(leaderIds.get(threadIndex % leaderIds.size()),
                                                                         groupId,
                                                                         memberId,
                                                                         threadIndex % 2 == 0);

                    long acquireTimestamp = System.currentTimeMillis();
                    log.info("[{}-thread] Acquired lock at: {}", threadIndex, acquireTimestamp);

                    methodCallSuccessThreads.add(threadIndex);
                    log.info("[{}-thread] Method call success", threadIndex);
                    if (flag && firstUpdatedThreadIndex.get() == 0) {
                        firstUpdatedThreadIndex.set(threadIndex);
                        log.info("[{}-thread] First updated the group membership", threadIndex);
                    }
                } catch (GroupMembershipException e) {
                    log.info("[{}-thread] Entity not found: {}", threadIndex, e.getMessage());
                } catch (RuntimeException e) {
                    log.info("[{}-thread] Lock acquisition failed: {}", threadIndex, e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        //Then
        log.info("Total successfully method call count: {}", methodCallSuccessThreads.size());
        int firstIndex = firstUpdatedThreadIndex.get();

        assertThat(methodCallSuccessThreads).contains(firstIndex);

        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            GroupMembership updatedGroupMembership = em.find(GroupMembership.class, GroupMembershipId.builder()
                                                                                                     .memberId(memberId)
                                                                                                     .groupId(groupId)
                                                                                                     .build());

            assertThat(updatedGroupMembership.getStatus())
                    .isEqualTo(firstIndex % 2 == 0 ? MembershipStatus.APPROVED : MembershipStatus.REJECTED);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    @Test
    @DisplayName("[Normal] modifyGroupRole(): 여러 클라이언트에서 동시에 같은 Group ID와 Member ID로 모임 내 회원 권한 변경 시도")
    void modifyGroupRole() throws Exception {
        //Given+
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);

        List<AtomicReference<Member>> leaderRefs = List.of(new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>(),
                                                           new AtomicReference<>());
        AtomicReference<Member> memberRef = new AtomicReference<>();
        AtomicReference<Group>  groupRef  = new AtomicReference<>();

        Future<?> future = executorService.submit(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Category category = Category.builder().name("category").build();
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
                groupRef.set(group);

                for (int i = 1; i <= leaderRefs.size(); i++) {
                    Member member = Member.builder()
                                          .username("testLeaderUsername%d".formatted(i))
                                          .password("testLeaderPassword%d".formatted(i))
                                          .nickname("testLeaderNickname%d".formatted(i))
                                          .build();
                    em.persist(member);
                    GroupMembership groupMembership = GroupMembership.builder()
                                                                     .member(member)
                                                                     .group(group)
                                                                     .groupRole(GroupRole.LEADER)
                                                                     .build();
                    em.persist(groupMembership);
                    leaderRefs.get(i - 1).set(member);
                }
                Member member = Member.builder()
                                      .username("testUsername")
                                      .password("testPassword")
                                      .nickname("testNickname")
                                      .build();
                em.persist(member);
                memberRef.set(member);
                GroupMembership groupMembership = GroupMembership.builder()
                                                                 .member(member)
                                                                 .group(group)
                                                                 .groupRole(GroupRole.PARTICIPANT)
                                                                 .build();
                groupMembership.modifyStatus(MembershipStatus.APPROVED);
                em.persist(groupMembership);

                transactionManager.commit(transactionStatus);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            }
        });
        future.get();

        List<Member> leaders = leaderRefs.stream().map(AtomicReference::get).toList();
        Group        group   = groupRef.get();
        Member       member  = memberRef.get();

        Long       groupId   = group.getId();
        List<Long> leaderIds = leaders.stream().map(Member::getId).toList();
        Long       memberId  = member.getId();

        //When
        Set<Integer>  methodCallSuccessThreads = ConcurrentHashMap.newKeySet();
        AtomicInteger lastUpdatedThreadIndex   = new AtomicInteger();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    Thread.sleep(10);
                    groupMembershipService.modifyGroupRole(leaderIds.get(threadIndex % leaderIds.size()),
                                                           groupId,
                                                           memberId);

                    long acquireTimestamp = System.currentTimeMillis();
                    log.info("[{}-thread] Acquired lock at: {}", threadIndex, acquireTimestamp);

                    methodCallSuccessThreads.add(threadIndex);
                    log.info("[{}-thread] Method call success", threadIndex);
                    lastUpdatedThreadIndex.set(threadIndex);
                } catch (RuntimeException e) {
                    log.info("[{}-thread] Lock acquisition failed: {}", threadIndex, e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        //Then
        log.info("Total successfully method call count: {}", methodCallSuccessThreads.size());
        int lastIndex = lastUpdatedThreadIndex.get();

        assertThat(methodCallSuccessThreads).contains(lastIndex);

        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            GroupMembership updatedGroupMembership = em.find(GroupMembership.class, GroupMembershipId.builder()
                                                                                                     .memberId(memberId)
                                                                                                     .groupId(groupId)
                                                                                                     .build());

            assertThat(updatedGroupMembership.getGroupRole())
                    .isEqualTo(methodCallSuccessThreads.size() % 2 == 0 ? GroupRole.PARTICIPANT : GroupRole.LEADER);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    @Test
    @DisplayName("[Normal] leaveGroup(): 여러 클라이언트에서 동시에 같은 Group ID와 Member ID로 모임 탈퇴 시도")
    void leaveGroup() throws Exception {
        //Given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT + 1);
        CountDownLatch  countDownLatch  = new CountDownLatch(THREAD_COUNT);

        AtomicReference<Member> memberRef = new AtomicReference<>();
        AtomicReference<Group>  groupRef  = new AtomicReference<>();

        Future<?> future = executorService.submit(() -> {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Category category = Category.builder().name("category").build();
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
                groupRef.set(group);

                Member leader = Member.builder()
                                      .username("testLeaderUsername")
                                      .password("testLeaderPassword")
                                      .nickname("testLeaderNickname")
                                      .build();
                em.persist(leader);
                GroupMembership groupLeaderMembership = GroupMembership.builder()
                                                                       .member(leader)
                                                                       .group(group)
                                                                       .groupRole(GroupRole.LEADER)
                                                                       .build();
                em.persist(groupLeaderMembership);

                Member member = Member.builder()
                                      .username("testUsername")
                                      .password("testPassword")
                                      .nickname("testNickname")
                                      .build();
                em.persist(member);
                memberRef.set(member);
                GroupMembership groupMembership = GroupMembership.builder()
                                                                 .member(member)
                                                                 .group(group)
                                                                 .groupRole(GroupRole.PARTICIPANT)
                                                                 .build();
                groupMembership.modifyStatus(MembershipStatus.APPROVED);
                em.persist(groupMembership);

                transactionManager.commit(transactionStatus);
            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw e;
            }
        });
        future.get();

        Group  group  = groupRef.get();
        Member member = memberRef.get();

        Long groupId  = group.getId();
        Long memberId = member.getId();

        //When
        Set<Integer>  methodCallSuccessThreads = ConcurrentHashMap.newKeySet();
        AtomicInteger firstDeletedThreadIndex  = new AtomicInteger();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            int threadIndex = i;
            executorService.execute(() -> {
                try {
                    Thread.sleep(10);
                    boolean flag = groupMembershipService.leaveGroup(groupId, memberId);

                    long acquireTimestamp = System.currentTimeMillis();
                    log.info("[{}-thread] Acquired lock at: {}", threadIndex, acquireTimestamp);

                    methodCallSuccessThreads.add(threadIndex);
                    log.info("[{}-thread] Method call success", threadIndex);
                    if (flag && firstDeletedThreadIndex.get() == 0) {
                        firstDeletedThreadIndex.set(threadIndex);
                        log.info("[{}-thread] First deleted the group membership", threadIndex);
                    }
                } catch (GroupMembershipException e) {

                } catch (RuntimeException e) {
                    log.info("[{}-thread] Lock acquisition failed: {}", threadIndex, e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        //Then
        log.info("Total successfully method call count: {}", methodCallSuccessThreads.size());
        int firstIndex = firstDeletedThreadIndex.get();

        assertThat(methodCallSuccessThreads).contains(firstIndex);

        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            GroupMembership updatedGroupMembership = em.find(GroupMembership.class, GroupMembershipId.builder()
                                                                                                     .memberId(memberId)
                                                                                                     .groupId(groupId)
                                                                                                     .build());

            assertThat(updatedGroupMembership.getStatus()).isEqualTo(MembershipStatus.LEAVE);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

}