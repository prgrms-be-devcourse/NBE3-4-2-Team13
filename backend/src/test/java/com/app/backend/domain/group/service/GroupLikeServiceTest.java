package com.app.backend.domain.group.service;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.repository.GroupLikeRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class GroupLikeServiceTest {

    @Autowired
    private GroupLikeService groupLikeService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupLikeRepository groupLikeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("그룹 좋아요")
    void t1() throws Exception {
        // 그룹, 멤버 개별 생성
        Category category = categoryRepository.save(Category.builder().name("카테고리").build());
        Group group = groupRepository.save(
                Group.builder()
                        .name("test group")
                        .province("test province")
                        .city("test city")
                        .town("test town")
                        .description("test description")
                        .recruitStatus(RecruitStatus.RECRUITING)
                        .maxRecruitCount(300)
                        .category(category)
                        .build());
        Member member = memberRepository.save(Member.builder().username("user0").build());

        Long memberId = member.getId();
        Long groupId = group.getId();

        groupLikeService.toggleLikeGroup(groupId, memberId);

        long likeCount = groupLikeRepository.countByGroupIdAndMemberId(groupId, memberId);
        assertThat(likeCount).isEqualTo(1);
    }


    @Test
    @DisplayName("동일 유저가 같은 그룹에 대해 좋아요를 눌렀다가 취소했을 때, likeCount ＝ ０")
    void t2() throws Exception {
        // 그룹, 멤버 개별 생성
        Category category = categoryRepository.save(Category.builder().name("카테고리").build());
        Group group = groupRepository.save(Group.builder()
                .name("test group")
                .province("test province")
                .city("test city")
                .town("test town")
                .description("test description")
                .recruitStatus(RecruitStatus.RECRUITING)
                .maxRecruitCount(300)
                .category(category)
                .build());
        Member member = memberRepository.save(Member.builder().username("user0").build());

        Long memberId = member.getId();
        Long groupId = group.getId();

        groupLikeService.toggleLikeGroup(groupId, memberId);
        groupLikeService.toggleLikeGroup(groupId, memberId);

        long likeCount = groupLikeRepository.countByGroupIdAndMemberId(groupId, memberId);
        assertThat(likeCount).isEqualTo(0);
    }

    @Test
    @DisplayName("동일 유저가 같은 그룹에 대해 동시에 좋아요를 눌렀을 때, likeCount ＝ ０")
    void t3() throws Exception {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 그룹, 멤버 개별 생성
        Category category = categoryRepository.save(Category.builder().name("카테고리").build());
        Group group = groupRepository.save(Group.builder()
                .name("test group")
                .province("test province")
                .city("test city")
                .town("test town")
                .description("test description")
                .recruitStatus(RecruitStatus.RECRUITING)
                .maxRecruitCount(300)
                .category(category)
                .build());
        Member member = memberRepository.save(Member.builder().username("user0").build());

        Long memberId = member.getId();
        Long groupId = group.getId();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    groupLikeService.toggleLikeGroup(groupId, memberId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        long likeCount = groupLikeRepository.countByGroupIdAndMemberId(groupId, memberId);
        assertThat(likeCount).isEqualTo(0L);
    }

}


