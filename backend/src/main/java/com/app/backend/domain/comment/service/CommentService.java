package com.app.backend.domain.comment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.comment.dto.request.CommentCreateRequest;
import com.app.backend.domain.comment.dto.response.CommentResponse;
import com.app.backend.domain.comment.entity.Comment;
import com.app.backend.domain.comment.exception.CommentErrorCode;
import com.app.backend.domain.comment.exception.CommentException;
import com.app.backend.domain.comment.repository.CommentRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.exception.PostErrorCode;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.repository.post.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final MemberRepository memberRepository;

	// 댓글 작성
	@Transactional
	public CommentResponse createComment(Long postId, Long memberId, CommentCreateRequest req) {

		//댓글 내용이 없으면 댓글 작성 실패
		if (req.getContent() == null || req.getContent().trim().isEmpty()) {
			throw new CommentException(CommentErrorCode.COMMENT_INVALID_CONTENT);
		}

		//게시물 조회
		Post post = postRepository.findByIdAndDisabled(postId, false)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));


		//사용자 조회
		Member member = memberRepository.findByIdAndDisabled(memberId, false)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


		Comment comment = Comment.builder()
			.content(req.getContent())
			.post(post)
			.member(member)
			.build();

		commentRepository.save(comment);

		return CommentResponse.from(comment);

	}

	// 댓글 삭제
	@Transactional
	public void deleteComment(Long commentId, Long memberId) {

		// 댓글 조회
		Comment comment = commentRepository.findByIdAndDisabled(commentId, false)
			.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));


		// 댓글 작성자만 삭제 가능
		if (!comment.getMember().getId().equals(memberId)) {
			throw new CommentException(CommentErrorCode.COMMENT_ACCESS_DENIED);
		}

		comment.delete();
	}
}
