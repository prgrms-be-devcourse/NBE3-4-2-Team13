package com.app.backend.domain.comment.dto.response;

import java.time.LocalDateTime;

import com.app.backend.domain.comment.entity.Comment;

import lombok.Builder;
import lombok.Getter;

public class CommentResponse {

	@Getter
	@Builder
	public static class CommentList {
		private Long id;
		private String content;
		private Long memberId;
		private String nickname;
		private LocalDateTime createdAt;
		private int replyCount;

		public static CommentList from(Comment comment) {
			return CommentList.builder()
				.id(comment.getId())
				.content(comment.getContent())
				.memberId(comment.getMember().getId())
				.nickname(comment.getMember().getNickname())
				.createdAt(comment.getCreatedAt())
				.replyCount(comment.getChildren().size())
				.build();
		}
	}

	@Getter
	@Builder
	public static class ReplyList {
		private Long id;
		private String content;
		private Long postId;
		private Long memberId;
		private String nickname;
		private Long parentId;
		private LocalDateTime createdAt;
		private LocalDateTime modifiedAt;

		public static ReplyList from(Comment reply) {
			return ReplyList.builder()
				.id(reply.getId())
				.content(reply.getContent())
				.postId(reply.getPost().getId())
				.memberId(reply.getMember().getId())
				.nickname(reply.getMember().getNickname())
				.parentId(reply.getParent().getId())
				.createdAt(reply.getCreatedAt())
				.modifiedAt(reply.getModifiedAt())
				.build();
		}
	}


	@Getter
	@Builder
	public static class CommentDto {
		private Long id;
		private String content;
		private Long postId;
		private Long memberId;
		private String nickname;
		private LocalDateTime createdAt;
		private LocalDateTime modifiedAt;
		private int replyCount;

		public static CommentDto from(Comment comment) {
			return CommentDto.builder()
				.id(comment.getId())
				.content(comment.getContent())
				.postId(comment.getPost().getId())
				.memberId(comment.getMember().getId())
				.nickname(comment.getMember().getNickname())
				.createdAt(comment.getCreatedAt())
				.modifiedAt(comment.getModifiedAt())
				.replyCount(comment.getChildren().size())
				.build();
		}
	}

	@Getter
	@Builder
	public static class ReplyDto {
		private Long id;
		private String content;
		private Long memberId;
		private String nickname;
		private Long parentId;
		private LocalDateTime createdAt;
		private LocalDateTime modifiedAt;

		public static ReplyDto from(Comment reply) {
			return ReplyDto.builder()
				.id(reply.getId())
				.content(reply.getContent())
				.memberId(reply.getMember().getId())
				.nickname(reply.getMember().getNickname())
				.parentId(reply.getParent().getId())
				.createdAt(reply.getCreatedAt())
				.modifiedAt(reply.getModifiedAt())
				.build();
		}
	}
}
