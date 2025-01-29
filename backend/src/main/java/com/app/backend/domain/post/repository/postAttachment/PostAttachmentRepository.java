package com.app.backend.domain.post.repository.postAttachment;

import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.post.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long>, PostAttachmentRepositoryCustom {

    List<PostAttachment> findByPostId(Long postId);

    List<PostAttachment> findByPostIdAndFileTypeOrderByCreatedAtDesc(Long postId, FileType fileType);

}
