package com.app.backend.domain.group.controller;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.repository.GroupLikeRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.group.service.GroupLikeService;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.global.annotation.CustomWithMockUser;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GroupLikeControllerTest {

    @MockitoBean
    private GroupLikeService groupLikeService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupLikeRepository groupLikeRepository;

    @Autowired
    MockMvc mvc;

    private Group group;
    private Member member;
    private Category category;

    @BeforeEach
    void setUp() {
        groupRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("category")
                .build());

        group = groupRepository.save(Group.builder()
                .name("test group")
                .province("test province")
                .city("test city")
                .town("test town")
                .description("test description")
                .recruitStatus(RecruitStatus.RECRUITING)
                .maxRecruitCount(10)
                .category(category)
                .build());

        member = memberRepository.save(Member.builder()
                .username("testUser")
                .nickname("testNickname")
                .role("USER")
                .disabled(false)
                .build());
    }

    @Test
    @DisplayName("그룹 좋아요 - 성공")
    @CustomWithMockUser
    void toggleLikeGroup_Success() throws Exception {
        // given
        doNothing().when(groupLikeService).likeGroup(group.getId(), member.getId());

        // when
        mvc.perform(post("/api/v1/groups/" + group.getId() + "/like")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.isSuccess").value(true))
                        .andExpect(jsonPath("$.message").value("좋아요 성공"));
    }

    @Test
    @DisplayName("그룹 좋아요 취소 - 성공")
    @CustomWithMockUser
    void toggleLikeGroup_Cancel() throws Exception {
        // given
        doNothing().when(groupLikeService).likeGroup(group.getId(), member.getId());
        doNothing().when(groupLikeService).unlikeGroup(group.getId(), member.getId());

        // when
        mvc.perform(delete("/api/v1/groups/{groupId}/like", group.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.message").value("좋아요 취소 성공"));
    }
}
