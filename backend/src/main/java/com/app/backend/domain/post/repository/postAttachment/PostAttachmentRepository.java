package com.app.backend.domain.post.repository.postAttachment;

import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.post.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long>, PostAttachmentRepositoryCustom {

    List<PostAttachment> findByPostIdAndDisabled(Long postId, Boolean disabled);

    List<PostAttachment> findByPostIdAndFileTypeAndDisabledOrderByCreatedAtDesc(Long postId, FileType fileType, Boolean disabled);

    List<PostAttachment> findByPostIdAndFileTypeOrderByCreatedAtDesc(Long postId, FileType fileType);

}
