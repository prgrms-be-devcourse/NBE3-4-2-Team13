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

	private MemberDetails memberDetails;
	private Comment parentComment;
	private Comment testReply;

	@BeforeEach
	void setUp() {
		// 테스트용 멤버 생성
		Member testMember = Member.builder()
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
	@DisplayName("대댓글 작성 실패 (부모 댓글 없음)")
	void createReply3() throws Exception {

		CommentCreateRequest request = new CommentCreateRequest("test");

		ResultActions resultActions = mvc
			.perform(
				post("/api/v1/comment/0/reply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.with(user(memberDetails))
			)
			.andDo(print());

		resultActions
			.andExpect(handler().handlerType(CommentController.class))
			.andExpect(handler().methodName("createReply"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CM001"))
			.andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다"));
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

}