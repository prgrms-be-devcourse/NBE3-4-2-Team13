package com.app.backend.domain.comment.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.comment.dto.request.CommentCreateRequest;
import com.app.backend.domain.comment.entity.Comment;
import com.app.backend.domain.comment.repository.CommentRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CommentControllerReplyTest {

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

	private Member testMember;
	private Post testPost;
	private MemberDetails memberDetails;
	private Comment parentComment;
	private Comment testReply;

	@BeforeEach
	void setUp() {
		// 테스트용 멤버 생성
		testMember = Member.builder()
			.username("testUser")
			.password("password")
			.nickname("테스터")
			.role("USER")
			.disabled(false)
			.build();
		memberRepository.save(testMember);
		memberDetails = new MemberDetails(testMember);

		// 테스트용 게시물 생성
		Post testPost = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .postStatus(PostStatus.PUBLIC)
                .groupId(1L)
                .memberId(testMember.getId())
                .nickName("테스트 닉").build();

		testPost = postRepository.save(testPost);

		// 테스트용 댓글 생성
		parentComment = Comment.builder()
			.content("부모 댓글")
			.post(testPost)
			.member(testMember)
			.build();
		parentComment = commentRepository.save(parentComment);

		// 테스트용 대댓글 생성
		testReply = Comment.builder()
			.content("test reply")
			.post(testPost)
			.member(testMember)
			.parent(parentComment)
			.build();
		testReply = commentRepository.save(testReply);
		parentComment.addReply(testReply);
	}

	@Test
	@DisplayName("대댓글 작성 성공")
	void createReply() throws Exception {

		CommentCreateRequest request = new CommentCreateRequest("test");

		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/comment/" + parentComment.getId() + "/reply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("createReply"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("201"))
			.andExpect(jsonPath("$.data.content").value("test"));
	}

	@Test
	@DisplayName("대댓글 작성 실패 (내용 없음)")
	void createReply2() throws Exception {

		CommentCreateRequest request = new CommentCreateRequest("");

		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/comment/" + parentComment.getId() + "/reply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());
		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("createReply"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM004"))
			.andExpect(jsonPath("$.message").value("댓글 내용이 유효하지 않습니다"));
	}

	@Test
	@DisplayName("대댓글 작성 성공 (부모 댓글이 삭제되어도 작성 가능)")
	void createReply3() throws Exception {

		parentComment.deactivate();
		commentRepository.save(parentComment);

		Assertions.assertThat(parentComment.getDisabled()).isTrue();

		CommentCreateRequest request = new CommentCreateRequest("test");

		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/comment/" + parentComment.getId() + "/reply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("createReply"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("201"))
			.andExpect(jsonPath("$.data.content").value("test"));
	}

	@Test
	@DisplayName("대댓글 수정 성공")
	void updateReply() throws Exception {

		CommentCreateRequest request = new CommentCreateRequest("수정된 내용");

		ResultActions resultActions = mvc
			.perform(
				patch("/api/v1/comment/" + testReply.getId() + "/reply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").exists())
			.andExpect(jsonPath("$.data.content").value("수정된 내용"));
	}


	@Test
	@DisplayName("대댓글 수정 실패 (작성자가 아닌 경우)")
	void updateReply2() throws Exception {

		Member otherMember = memberRepository.save(Member.builder()
			.username("other")
			.password("password")
			.nickname("다른사용자")
			.role("USER")
			.build());

		CommentCreateRequest request = new CommentCreateRequest("수정된 내용");

		mvc.perform(
			patch("/api/v1/comment/" + testReply.getId() + "/reply")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(user(new MemberDetails(otherMember)))
		)
			.andDo(print())

			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM003"))
			.andExpect(jsonPath("$.message").value("댓글에 대한 권한이 없습니다"));
	}

	@Test
	@DisplayName("대댓글 수정 실패 (내용 없음)")
	void updateReply3() throws Exception {

		CommentCreateRequest request = new CommentCreateRequest("");

		mvc.perform(
				patch("/api/v1/comment/" + testReply.getId() + "/reply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print())

			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM004"))
			.andExpect(jsonPath("$.message").value("댓글 내용이 유효하지 않습니다"));
	}

	@Test
	@DisplayName("대댓글 수정 성공(부모 댓글이 삭제되어도 수정 가능)")
	void updateReply4() throws Exception {

		parentComment.deactivate();
		commentRepository.save(parentComment);

		Assertions.assertThat(parentComment.getDisabled()).isTrue();

		CommentCreateRequest request = new CommentCreateRequest("수정된 내용");

		ResultActions resultActions = mvc
			.perform(
				patch("/api/v1/comment/" + testReply.getId() + "/reply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").exists())
			.andExpect(jsonPath("$.data.content").value("수정된 내용"));
	}


	@Test
	@DisplayName("대댓글 삭제 성공")
	void deleteReply() throws Exception {

		mvc.perform(
				delete("/api/v1/comment/" + testReply.getId() + "/reply")
					.with(user(memberDetails))
			)
			.andDo(print())

			.andExpect(status().isNoContent())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("204"))
			.andExpect(jsonPath("$.message").value("%d번 답글이 삭제되었습니다.".formatted(testReply.getId())));
	}

	@Test
	@DisplayName("대댓글 삭제 실패 (작성자가 아닌 경우)")
	void deleteReply2() throws Exception {

		Member otherMember = memberRepository.save(Member.builder()
			.username("other")
			.password("password")
			.nickname("다른사용자")
			.role("USER")
			.build());

		mvc.perform(
				delete("/api/v1/comment/" + testReply.getId() + "/reply")
					.with(user(new MemberDetails(otherMember)))
			)
			.andDo(print())


			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM003"))
			.andExpect(jsonPath("$.message").value("댓글에 대한 권한이 없습니다"));
	}

	@Test
	@DisplayName("대댓글 삭제 성공(부모 댓글이 삭제되어도 삭제 가능)")
	void deleteReply3() throws Exception {

		parentComment.deactivate();
		commentRepository.save(parentComment);

		Assertions.assertThat(parentComment.getDisabled()).isTrue();

		ResultActions resultActions = mvc
			.perform(
				delete("/api/v1/comment/" + testReply.getId() + "/reply")
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isNoContent())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("204"))
			.andExpect(jsonPath("$.message").value("%d번 답글이 삭제되었습니다.".formatted(testReply.getId())));

	}


	@Test
	@DisplayName("대댓글 페이징 조회 성공 (대댓글 여러개)")
	void getReplies() throws Exception {

		for (int i = 1; i <= 14; i++) {
			Comment reply = Comment.builder()
				.content("reply" + i)
				.post(testPost)
				.member(testMember)
				.parent(parentComment)
				.build();
			reply = commentRepository.save(reply);
			parentComment.addReply(reply);
		}

		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/comment/" + parentComment.getId() + "/reply")
					.param("page", "0")
					.param("size", "10")
					.param("sort", "id,desc")
					.contentType(MediaType.APPLICATION_JSON)
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").exists())
			.andExpect(jsonPath("$.data.content.length()").value(10))
			.andExpect(jsonPath("$.data.totalElements").value(15))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.hasNext").value(true))
			.andExpect(jsonPath("$.data.isLast").value(false));

		ResultActions resultActions2 = mvc
			.perform(
				get("/api/v1/comment/" + parentComment.getId() + "/reply")
					.param("page", "1")
					.param("size", "10")
					.param("sort", "id,desc")
					.contentType(MediaType.APPLICATION_JSON)
					.with(user(memberDetails))

			)
			.andDo(print());

		resultActions2
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").exists())
			.andExpect(jsonPath("$.data.content.length()").value(5))
			.andExpect(jsonPath("$.data.totalElements").value(15))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.isLast").value(true));
	}

	@Test
	@DisplayName("대댓글 페이징 조회 성공 (댓글 삭제되어도 조회 가능)")
	void getReplies2() throws Exception {

		for (int i = 1; i <= 14; i++) {
			Comment reply = Comment.builder()
				.content("reply" + i)
				.post(testPost)
				.member(testMember)
				.parent(parentComment)
				.build();
			reply = commentRepository.save(reply);
			parentComment.addReply(reply);
		}

		parentComment.deactivate();
		commentRepository.save(parentComment);

		Assertions.assertThat(parentComment.getDisabled()).isTrue();

		ResultActions resultActions = mvc
			.perform(
				get("/api/v1/comment/" + parentComment.getId() + "/reply")
					.param("page", "0")
					.param("size", "10")
					.param("sort", "id,desc")
					.contentType(MediaType.APPLICATION_JSON)
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").exists())
			.andExpect(jsonPath("$.data.content.length()").value(10))
			.andExpect(jsonPath("$.data.totalElements").value(15))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.hasNext").value(true))
			.andExpect(jsonPath("$.data.isLast").value(false));

		ResultActions resultActions2 = mvc
			.perform(
				get("/api/v1/comment/" + parentComment.getId() + "/reply")
					.param("page", "1")
					.param("size", "10")
					.param("sort", "id,desc")
					.contentType(MediaType.APPLICATION_JSON)
					.with(user(memberDetails))

			)
			.andDo(print());

		resultActions2
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").exists())
			.andExpect(jsonPath("$.data.content.length()").value(5))
			.andExpect(jsonPath("$.data.totalElements").value(15))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.isLast").value(true));
	}

}