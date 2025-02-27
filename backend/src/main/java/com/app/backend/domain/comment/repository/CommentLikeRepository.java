package com.app.backend.domain.comment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.app.backend.domain.comment.entity.CommentLike;

import jakarta.persistence.LockModeType;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<CommentLike> findByCommentIdAndMemberIdAndDisabled(Long commentId, Long memberId, boolean disabled);

}
