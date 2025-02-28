package com.app.backend.domain.post.repository.post;

import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepositoryCustom {

    Page<Post> findAllBySearchStatus(Long groupId, String search, PostStatus postStatus, boolean disabled, Pageable pageable);

    Page<Post> findAllByUserAndSearchStatus(Long groupId, Long memberId, String search, PostStatus postStatus, boolean disabled, Pageable pageable);

    List<Post> findPostsByGroupIdOrderByTodayViewsCountDesc(Long groupId, int limit, boolean disabled);

    void deleteAllByModifiedAtAndDisabled(LocalDateTime lastModified, boolean disabled);
}
