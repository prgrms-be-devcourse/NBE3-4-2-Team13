package com.app.backend.domain.comment.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.comment.dto.request.CommentCreateRequest;
import com.app.backend.domain.comment.entity.Comment;
import com.app.backend.domain.comment.entity.CommentLike;
import com.app.backend.domain.comment.repository.CommentLikeRepository;
import com.app.backend.domain.comment.repository.CommentRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.global.annotation.CustomWithMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class CommentControllerTest {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private CommentLikeRepository commentLikeRepository;

	private Post testPost;
	private Long testPostId;
	private Member testMember;
	private MemberDetails memberDetails;

	@BeforeEach
	void setUp() {

		// 테스트용
		testMember = Member.builder()
			.username("testUser")
			.password("password")
			.nickname("테스터")
			.role("USER")
			.disabled(false)
			.build();

		memberRepository.save(testMember);
		memberDetails = new MemberDetails(testMember);

		testPost = Post.builder()
				.title("테스트 게시글")
				.content("테스트 내용")
				.postStatus(PostStatus.PUBLIC)
				.groupId(1L)
				.memberId(testMember.getId())
				.nickName("테스트 닉")
			.build();

		testPost = postRepository.save(testPost);
		testPostId = testPost.getId();

	}

	@Test
	@DisplayName("댓글 작성")
	void createComment() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest("test");

		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/comment/" + testPostId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("createComment"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("201"))
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@DisplayName("댓글 작성 실패 (내용 공백)")
	void createComment2() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest("");

		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/comment/" + testPostId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("createComment"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM004"))
			.andExpect(jsonPath("$.message").value("댓글 내용이 유효하지 않습니다"));
	}

	@Test
	@DisplayName("댓글 삭제")
	void deleteComment() throws Exception {
		Comment testComment = Comment.builder()
			.content("test")
			.post(testPost)
			.member(testMember)
			.build();
		testComment = commentRepository.save(testComment);

		ResultActions resultActions = mvc
			.perform(
				delete("/api/v1/comment/" + testComment.getId())
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("deleteComment"))
			.andExpect(status().isNoContent())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("204"))
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@DisplayName("댓글 삭제 실패 (존재하지 않는 댓글)")
	void deleteComment2() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				delete("/api/v1/comment/10000")
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("deleteComment"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM001"))
			.andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다"));
	}

	@Test
	@DisplayName("댓글 삭제 실패 (작성자만 삭제 가능)")
	void deleteComment3() throws Exception {
		Comment testComment = Comment.builder()
			.content("test")
			.post(testPost)
			.member(testMember)
			.build();
		testComment = commentRepository.save(testComment);

		// 다른 사용자 생성
		Member Member2 = Member.builder()
			.username("other")
			.password("password")
			.nickname("다른사용자")
			.role("USER")
			.build();

		memberRepository.save(Member2);
		MemberDetails otherMemberDetails = new MemberDetails(Member2);

		ResultActions resultActions = mvc
			.perform(
				delete("/api/v1/comment/" + testComment.getId())
					.with(user(otherMemberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("deleteComment"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM003"))
			.andExpect(jsonPath("$.message").value("댓글에 대한 권한이 없습니다"));
	}

	@Test
	@DisplayName("댓글 수정")
	void updateComment() throws Exception {
		Comment testComment = Comment.builder()
			.content("test")
			.post(testPost)
			.member(testMember)
			.build();
		testComment = commentRepository.save(testComment);

		CommentCreateRequest request = new CommentCreateRequest("수정 댓글");

		ResultActions resultActions = mvc
			.perform(
				patch("/api/v1/comment/" + testComment.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("updateComment"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").exists());
	}

	@Test
	@DisplayName("댓글 수정 실패 (내용 공백)")
	void updateComment2() throws Exception {
		Comment testComment = Comment.builder()
			.content("test")
			.post(testPost)
			.member(testMember)
			.build();
		testComment = commentRepository.save(testComment);

		CommentCreateRequest request = new CommentCreateRequest("");

		ResultActions resultActions = mvc
			.perform(
				patch("/api/v1/comment/" + testComment.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("updateComment"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM004"))
			.andExpect(jsonPath("$.message").value("댓글 내용이 유효하지 않습니다"));
	}

	@Test
	@DisplayName("댓글 수정 실패 (존재하지 않는 댓글)")
	void updateComment3() throws Exception {
		CommentCreateRequest request = new CommentCreateRequest("수정 댓글");

		ResultActions resultActions = mvc
			.perform(
				patch("/api/v1/comment/10000")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("updateComment"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM001"))
			.andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다"));
	}

	@Test
	@DisplayName("댓글 수정 실패 (작성자만 수정 가능)")
	void updateComment4() throws Exception {
		Comment testComment = Comment.builder()
			.content("test")
			.post(testPost)
			.member(testMember)
			.build();
		testComment = commentRepository.save(testComment);

		Member Member2 = Member.builder()
			.username("other")
			.password("password")
			.nickname("다른사용자")
			.role("USER")
			.build();
		memberRepository.save(Member2);
		MemberDetails otherMemberDetails = new MemberDetails(Member2);

		CommentCreateRequest request = new CommentCreateRequest("수정 댓글");

		ResultActions resultActions = mvc
			.perform(
				patch("/api/v1/comment/" + testComment.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(otherMemberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("updateComment"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM003"))
			.andExpect(jsonPath("$.message").value("댓글에 대한 권한이 없습니다"));
	}

	@Test
	@DisplayName("댓글 페이징 조회")
	@CustomWithMockUser(role="USER")
	void getCommentsWithPaging() throws Exception {

		for (int i = 1; i <= 15; i++) {
			Comment comment = Comment.builder()
				.content("test comment " + i)
				.post(testPost)
				.member(testMember)
				.build();
			commentRepository.save(comment);
		}

		mvc.perform(
				get("/api/v1/comment/" + testPostId)
					.param("page", "0")
					.param("size", "10")
					.param("sort", "createdAt,desc")
			)
			.andDo(print())

			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content.length()").value(10))
			.andExpect(jsonPath("$.data.totalElements").value(15))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.hasNext").value(true))
			.andExpect(jsonPath("$.data.isLast").value(false));

		mvc.perform(
				get("/api/v1/comment/" + testPostId)
					.param("page", "1")
					.param("size", "10")
					.param("sort", "createdAt,desc")
			)
			.andDo(print())

			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content.length()").value(5))
			.andExpect(jsonPath("$.data.totalElements").value(15))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.isLast").value(true));
	}

	@Test
	@DisplayName("댓글 페이징 조회 (게시물 없음)")
	@CustomWithMockUser(role="USER")
	void getComments3() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/comment/10000")
					.param("page", "0")
					.param("size", "10")
					.param("sort", "createdAt,desc")
			)
			.andDo(print());

		resultActions
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("P001"))
			.andExpect(jsonPath("$.message").value("게시물 정보가 존재하지 않음"));
	}

	@Test
	@DisplayName("댓글 페이징 조회 (댓글 없음)")
	@CustomWithMockUser(role="USER")
	void getComments4() throws Exception {
		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/comment/" + testPostId)
					.param("page", "0")
					.param("size", "10")
					.param("sort", "createdAt,desc")
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").exists())
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content.length()").value(0))
			.andExpect(jsonPath("$.data.totalElements").value(0))
			.andExpect(jsonPath("$.data.totalPages").value(0));
	}

	@DisplayName("댓글 조회 (좋아요 수)")
	@Test
	@CustomWithMockUser(role="USER")
	void getCommentsLike() throws Exception {

		Comment testComment = Comment.builder()
			.content("테스트 댓글")
			.post(testPost)
			.member(testMember)
			.build();
		commentRepository.save(testComment);

		for (int i = 0; i < 3; i++) {
			CommentLike like = CommentLike.builder()
				.comment(testComment)
				.member(testMember)
				.build();
			commentLikeRepository.save(like);
		}


		mvc.perform(get("/api/v1/comment/" + testPostId)
				.param("page", "0")
				.param("size", "10")
				.param("sort", "createdAt,desc")
				.contentType(MediaType.APPLICATION_JSON))

			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].likeCount").value(3))
			.andDo(print());
	}

}



