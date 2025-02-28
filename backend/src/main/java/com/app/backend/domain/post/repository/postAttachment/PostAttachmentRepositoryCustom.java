package com.app.backend.domain.post.repository.postAttachment;

import com.app.backend.domain.post.entity.PostAttachment;

import java.time.LocalDateTime;
import java.util.List;

public interface PostAttachmentRepositoryCustom {

    List<PostAttachment> findAllByModifiedAtAndDisabled(LocalDateTime lastModified, boolean disabled);

    void deleteByIdList(List<Long> idList);

    void deleteByPostId(Long postId);

    void deleteByFileIdList(List<Long> fileIdList);

}
