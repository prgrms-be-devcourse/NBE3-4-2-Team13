package com.app.backend.domain.comment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.app.backend.domain.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

	Optional<Comment> findByIdAndDisabled(Long id, Boolean disabled);

	Page<Comment> findByParentAndDisabled(Comment comment, boolean disabled, Pageable pageable);

}
