package com.app.backend.domain.comment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.backend.domain.comment.entity.Comment;

public interface CommentRepository  extends JpaRepository<Comment, Long> {
	Optional<Comment> findByIdAndDisabled(Long id, Boolean disabled);
}
