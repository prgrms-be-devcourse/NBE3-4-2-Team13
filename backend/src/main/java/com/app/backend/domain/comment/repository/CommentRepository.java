package com.app.backend.domain.comment.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.app.backend.domain.comment.entity.Comment;
import com.app.backend.domain.post.entity.Post;

public interface CommentRepository  extends JpaRepository<Comment, Long> {
	Optional<Comment> findByIdAndDisabled(Long id, Boolean disabled);

	Page<Comment> findByPostAndDisabledAndParentIsNull(Post post, boolean disabled, Pageable pageable);

	Page<Comment> findByParentAndDisabled(Comment comment, boolean b, Pageable pageable);
}
