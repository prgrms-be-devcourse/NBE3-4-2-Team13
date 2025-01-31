package com.app.backend.domain.post.repository.post;

import com.app.backend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<Post> findByIdAndDisabled(Long id, Boolean disabled);

}
