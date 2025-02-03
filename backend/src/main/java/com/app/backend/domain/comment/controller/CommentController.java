package com.app.backend.domain.comment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.backend.domain.comment.dto.request.CommentCreateRequest;
import com.app.backend.domain.comment.dto.response.CommentResponse;
import com.app.backend.domain.comment.service.CommentService;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.global.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comment")
public class CommentController {

	private final CommentService commentService;

	// 게시물에 대한 댓글 작성
	@PostMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<CommentResponse> createComment(@PathVariable(name = "id") Long postId,
		@RequestBody CommentCreateRequest req,
		@AuthenticationPrincipal MemberDetails memberDetails){

		CommentResponse response = commentService.createComment(postId, memberDetails.getId(), req);

		return ApiResponse.of(
			true,
			"201",
			"%d번 댓글이 작성되었습니다.".formatted(response.getId()),
			response
		);
	}

	//댓글 삭제
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> deleteComment(@PathVariable(name = "id") Long commentId,
		@AuthenticationPrincipal MemberDetails memberDetails){

		commentService.deleteComment(commentId, memberDetails.getId());

		return ApiResponse.of(
			true,
			"204",
			"%d번 댓글이 삭제되었습니다.".formatted(commentId)
		);
	}
}
