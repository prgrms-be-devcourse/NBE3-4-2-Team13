package com.app.backend.domain.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.backend.domain.comment.dto.response.CommentResponse;
import com.app.backend.domain.post.entity.Post;

public interface CommentRepositoryCustom {
	Page<CommentResponse.CommentList> findCommentsWithLikeCount(Post post, Pageable pageable);

}