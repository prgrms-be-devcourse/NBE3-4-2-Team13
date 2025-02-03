package com.app.backend.domain.post.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.post.dto.req.PostReqDto;
import com.app.backend.domain.post.dto.resp.PostRespDto;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.exception.PostErrorCode;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.service.PostService;
import com.app.backend.global.util.ReflectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Null;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    private static final String BASE_URL = "/api/v1/post";

    @Test
    @DisplayName("Success : 게시글 단건 조회")
    void getPost_Success() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .username("test")
                .nickname("test_name")
                .role("ROLE_USER")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("test_title")
                .content("test_content")
                .postStatus(PostStatus.PUBLIC)
                .memberId(1L)
                .groupId(1L)
                .build();

        ReflectionUtil.setPrivateFieldValue(Post.class, post, "createdAt", LocalDateTime.now());
        ReflectionUtil.setPrivateFieldValue(Post.class, post, "modifiedAt", LocalDateTime.now());

        MemberDetails mockUser = new MemberDetails(member);

        PostRespDto.GetPostDto responseDto = new PostRespDto.GetPostDto(post, member, null);

        given(postService.getPost(eq(post.getId()), eq(mockUser.getId()))).willReturn(responseDto);

        // When
        ResultActions resultActions = mockMvc.perform(get(BASE_URL + "/" + post.getId())
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.message").value("게시글을 성공적으로 불러왔습니다"));
        resultActions.andExpect(jsonPath("$.data.postId").value(1L));
        resultActions.andExpect(jsonPath("$.data.title").value("test_title"));
        resultActions.andExpect(jsonPath("$.data.content").value("test_content"));
        resultActions.andExpect(jsonPath("$.data.postStatus").value("PUBLIC"));
        resultActions.andExpect(jsonPath("$.data.nickName").value("test_name"));
        resultActions.andDo(print());
    }

    @Test
    @DisplayName("Fail : 게시글 단건 조회")
    void getPost_Fail() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .username("test")
                .nickname("test_name")
                .role("ROLE_USER")
                .build();

        MemberDetails mockUser = new MemberDetails(member);

        given(postService.getPost(2L, mockUser.getId())).willThrow(new PostException(PostErrorCode.POST_NOT_FOUND));

        // When
        ResultActions resultActions = mockMvc.perform(get(BASE_URL + "/" + 2)
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.message").value("게시물 정보가 존재하지 않음"));
        resultActions.andDo(print());
    }

    @Test
    @DisplayName("Success : 게시글 목록 조회")
    void getPosts_Success() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .username("test")
                .nickname("test_name")
                .role("ROLE_USER")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("test_title")
                .content("test_content")
                .postStatus(PostStatus.PUBLIC)
                .memberId(1L)
                .groupId(1L)
                .build();

        ReflectionUtil.setPrivateFieldValue(Post.class, post, "createdAt", LocalDateTime.now());
        ReflectionUtil.setPrivateFieldValue(Post.class, post, "modifiedAt", LocalDateTime.now());

        MemberDetails mockUser = new MemberDetails(member);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<PostRespDto.GetPostListDto> mockPage = new PageImpl<>(List.of(new PostRespDto.GetPostListDto(post)), pageable, 1);

        given(postService.getPostsBySearch(any(), any())).willReturn(mockPage);

        // When
        ResultActions resultActions = mockMvc.perform(get(BASE_URL)
                .with(user(mockUser))
                .param("groupId", "1")
                .param("keyword", "spring")
                .param("postStatus", "PUBLIC")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.content[0].postId").value(1L));
        resultActions.andExpect(jsonPath("$.message").value("게시물 목록을 성공적으로 불러왔습니다"));
        resultActions.andDo(print());
    }

    @Test
    @DisplayName("Success : 게시글 저장")
    void savePost_Success() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .username("test")
                .nickname("test_name")
                .role("ROLE_USER")
                .build();

        MemberDetails mockUser = new MemberDetails(member);

        PostReqDto.SavePostDto savePost = new PostReqDto.SavePostDto("Title", "Content", PostStatus.PUBLIC, 1L);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "dummy".getBytes()
        );

        MockMultipartFile postPart = new MockMultipartFile(
                "post",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(savePost)
        );

        Post mockPost = Post.builder().id(1L).build();

        given(postService.savePost(anyLong(), any(), any())).willReturn(mockPost);

        // When
        ResultActions resultActions = mockMvc.perform(multipart(BASE_URL)
                .file(file)
                .file(postPart)
                .with(user(mockUser))
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // Then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.postId").value(1L));
        resultActions.andExpect(jsonPath("$.message").value("게시글이 성공적으로 저장되었습니다"));
        resultActions.andDo(print());
    }

    @Test
    @DisplayName("Success : 게시글 수정")
    void updatePost_Success() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .username("test")
                .nickname("test_name")
                .role("ROLE_USER")
                .build();

        MemberDetails mockUser = new MemberDetails(member);

        PostReqDto.ModifyPostDto modifyPost =
                new PostReqDto.ModifyPostDto(1L, "title", "content", PostStatus.PUBLIC, 0L, null, null);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test2.jpg", "image/jpeg", "dummy".getBytes()
        );

        MockMultipartFile postPart = new MockMultipartFile(
                "post",
                "",
                "application/json",
                new ObjectMapper().writeValueAsBytes(modifyPost)
        );

        Post mockPost = Post.builder().id(1L).build();

        given(postService.updatePost(eq(mockUser.getId()), eq(1L), any(), any())).willReturn(mockPost);

        // When
        ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.PATCH, BASE_URL + "/" + 1)
                .file(file)
                .file(postPart)
                .with(user(mockUser))
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // Then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.postId").value(1L));
        resultActions.andExpect(jsonPath("$.message").value("게시글이 성공적으로 수정되었습니다"));
        resultActions.andDo(print());
    }

    @Test
    @DisplayName("Success : 게시글 삭제")
    void deletePost_Success() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .username("test")
                .nickname("test_name")
                .role("ROLE_USER")
                .build();

        MemberDetails mockUser = new MemberDetails(member);

        doNothing().when(postService).deletePost(eq(mockUser.getId()), eq(1L));

        // When & Then
        ResultActions resultActions = mockMvc.perform(delete(BASE_URL + "/" + 1)
                .with(user(mockUser)));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.message").value("게시글이 성공적으로 삭제되었습니다"));
        resultActions.andDo(print());
    }
}