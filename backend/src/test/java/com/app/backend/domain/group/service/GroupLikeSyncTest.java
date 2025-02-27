package com.app.backend.domain.group.service;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupLikeErrorCode;
import com.app.backend.domain.group.exception.GroupLikeException;
import com.app.backend.domain.group.repository.GroupLikeRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class GroupLikeSyncTest {

    @Autowired
    private GroupLikeService groupLikeService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GroupLikeRepository groupLikeRepository;

    @AfterEach
    void tearDown() {
        groupLikeRepository.deleteAll();
        groupRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("여러 유저가 동시에 같은 그룹에 대해 좋아요를 눌렀을 때, likeCount = 100")
    void multipleUsersLikeTest() throws Exception {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount);

        // given
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

        for (int i = 0; i < threadCount; i++) {
            Member member = memberRepository.save(Member.builder().username("user" + i).build());
            Long memberId = member.getId();

            executorService.submit(() -> {
                try {
                    groupLikeService.likeGroup(group.getId(), memberId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Group foundGroup = groupRepository.findById(group.getId())
                .orElseThrow(() -> new GroupLikeException(GroupLikeErrorCode.GROUP_NOT_FOUND));

        // then
        assertThat(foundGroup.getLikeCount()).isEqualTo(100L);
    }
}
