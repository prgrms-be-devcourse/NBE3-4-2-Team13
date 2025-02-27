package com.app.backend.domain.comment.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.comment.dto.request.CommentCreateRequest;
import com.app.backend.domain.comment.dto.response.CommentResponse;
import com.app.backend.domain.comment.entity.Comment;
import com.app.backend.domain.comment.entity.CommentLike;
import com.app.backend.domain.comment.exception.CommentErrorCode;
import com.app.backend.domain.comment.exception.CommentException;
import com.app.backend.domain.comment.repository.CommentLikeRepository;
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
	private final CommentLikeRepository commentLikeRepository;

	//댓글 조회
	private Comment getCommentValidate(Long id){
		return commentRepository.findByIdAndDisabled(id, false)
			.orElseThrow(()-> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
	}

	//게시물 조회
	private Post getPostValidate(Long postId){
		return postRepository.findByIdAndDisabled(postId, false)
			.orElseThrow(()-> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	//댓글 작성자만 수정과 삭제 가능
	private void validateAuthor(Comment comment, Long memberId) {
		if (!comment.getMember().getId().equals(memberId)) {
			throw new CommentException(CommentErrorCode.COMMENT_ACCESS_DENIED);
		}
	}

	//댓글 내용이 없으면 댓글 작성 실패
	private void validateCommentContent(String content) {
		if (content == null || content.trim().isEmpty()) {
			throw new CommentException(CommentErrorCode.COMMENT_INVALID_CONTENT);
		}
	}


	// 댓글 작성
	@Transactional
	public CommentResponse.CommentDto createComment(Long postId, Long memberId, CommentCreateRequest req) {

		validateCommentContent(req.getContent());

		Post post = getPostValidate(postId);

		//사용자 조회
		Member member = memberRepository.findByIdAndDisabled(memberId, false)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


		Comment comment = Comment.builder()
			.content(req.getContent())
			.post(post)
			.member(member)
			.build();

		commentRepository.save(comment);

		return CommentResponse.CommentDto.from(comment);

	}

	// 댓글 삭제
	@Transactional
	public void deleteComment(Long commentId, Long memberId) {

		Comment comment = getCommentValidate(commentId);

		validateAuthor(comment, memberId);

		comment.delete();
	}

	// 댓글 수정
	@Transactional
	public CommentResponse.CommentDto updateComment(Long commentId, Long memberId, CommentCreateRequest req) {

		Comment comment = getCommentValidate(commentId);

		validateCommentContent(req.getContent());

		validateAuthor(comment, memberId);

		comment.update(req.getContent());

		return CommentResponse.CommentDto.from(comment);


	}

	// 댓글 조회 (좋아요 수 포함)
	public Page<CommentResponse.CommentList> getComments(Long postId, Pageable pageable) {
		Post post = getPostValidate(postId);
		return commentRepository.findCommentsWithLikeCount(post, pageable);
	}


	// 대댓글 작성
	@Transactional
	public CommentResponse.ReplyDto createReply(Long commentId, Long memberId, CommentCreateRequest req) {

		Comment parentComment = getCommentValidate(commentId);

		Member member = memberRepository.findByIdAndDisabled(memberId, false)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


		validateCommentContent(req.getContent());


		Comment reply = Comment.builder()
			.content(req.getContent())
			.post(parentComment.getPost())
			.member(member)
			.parent(parentComment)
			.build();

		Comment saveReply = commentRepository.save(reply);

		parentComment.addReply(saveReply);

		return CommentResponse.ReplyDto.from(saveReply);
	}

	//대댓글 수정
	@Transactional
	public CommentResponse.ReplyDto updateReply(Long replyId, Long id, CommentCreateRequest req) {

		Comment reply = getCommentValidate(replyId);

		validateCommentContent(req.getContent());

		validateAuthor(reply, id);

		reply.update(req.getContent());

		return CommentResponse.ReplyDto.from(reply);
	}

	//대댓글 삭제
	@Transactional
	public void deleteReply(Long replyId, Long id) {

		Comment reply = getCommentValidate(replyId);

		validateAuthor(reply, id);

		Comment parent = reply.getParent();
		if (parent != null) {
			parent.removeReply(reply);

		}

		reply.delete();
	}

	//대댓글 조회
	public Page<CommentResponse.ReplyList> getReplies(Long commentId, Pageable pageable) {

		Comment comment = getCommentValidate(commentId);

		Page<Comment> replies = commentRepository.findByParentAndDisabled(comment, false, pageable);

		return replies.map(CommentResponse.ReplyList::from);
	}

	//댓글 좋아요
	@Transactional
	public void CommentLike(Long commentId, Long memberId) {

		Comment comment = getCommentValidate(commentId);

		Member member = memberRepository.findByIdAndDisabled(memberId, false)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


		//좋아요 확인
		Optional<CommentLike> isLike = commentLikeRepository.findByCommentIdAndMemberIdAndDisabled(commentId, memberId, false);

		if (isLike.isPresent()) {
			isLike.get().delete(); //좋아요가 있으면 삭제
		} else {
			CommentLike commentLike = CommentLike.builder()
				.comment(comment)
				.member(member)
				.build();

			commentLikeRepository.save(commentLike);
		}
	}

}
