package com.app.backend.domain.comment.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
	public ApiResponse<CommentResponse.CommentDto> createComment(@PathVariable(name = "id") Long postId,
		@RequestBody CommentCreateRequest req,
		@AuthenticationPrincipal MemberDetails memberDetails){

		CommentResponse.CommentDto response = commentService.createComment(postId, memberDetails.getId(), req);

		return ApiResponse.of(
			true,
			HttpStatus.CREATED,
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
			HttpStatus.NO_CONTENT,
			"%d번 댓글이 삭제되었습니다.".formatted(commentId)
		);
	}

	//댓글 수정
	@PatchMapping("/{id}")
	public ApiResponse<CommentResponse.CommentDto> updateComment(@PathVariable(name = "id") Long commentId,
		@RequestBody CommentCreateRequest req,
		@AuthenticationPrincipal MemberDetails memberDetails){

		CommentResponse.CommentDto response = commentService.updateComment(commentId, memberDetails.getId(),req);

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"%d번 댓글이 수정되었습니다.".formatted(commentId),
			response
		);
	}

	//게시물에 대한 댓글 조회 페이징
	@GetMapping("/{id}")
	public ApiResponse<Page<CommentResponse.CommentList>> getComments(
		@PathVariable(name = "id") Long postId,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<CommentResponse.CommentList> response = commentService.getComments(postId, pageable);

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"댓글이 조회되었습니다.",
			response
		);
	}

	//대댓글 작성
	@PostMapping("/{id}/reply")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<CommentResponse.ReplyDto> createReply(
		@PathVariable(name = "id") Long commentId,
		@RequestBody CommentCreateRequest req,
		@AuthenticationPrincipal MemberDetails memberDetails
	) {
		CommentResponse.ReplyDto response = commentService.createReply(commentId, memberDetails.getId(), req);

		return ApiResponse.of(
			true,
			HttpStatus.CREATED,
			"%d번 댓글에 대한 답글이 작성되었습니다.".formatted(commentId),
			response
		);
	}

	//대댓글 수정
	@PatchMapping("/{id}/reply")
	public ApiResponse<CommentResponse.ReplyDto> updateReply(
		@PathVariable(name = "id") Long replyId,
		@RequestBody CommentCreateRequest req,
		@AuthenticationPrincipal MemberDetails memberDetails
	) {
		CommentResponse.ReplyDto response = commentService.updateReply(replyId, memberDetails.getId(), req);

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"%d번 답글이 수정되었습니다.".formatted(replyId),
			response
		);
	}

	//대댓글 삭제
	@DeleteMapping("/{id}/reply")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> deleteReply(
		@PathVariable(name = "id") Long replyId,
		@AuthenticationPrincipal MemberDetails memberDetails
	) {
		commentService.deleteReply(replyId, memberDetails.getId());

		return ApiResponse.of(
			true,
			HttpStatus.NO_CONTENT,
			"%d번 답글이 삭제되었습니다.".formatted(replyId)
		);
	}
}
