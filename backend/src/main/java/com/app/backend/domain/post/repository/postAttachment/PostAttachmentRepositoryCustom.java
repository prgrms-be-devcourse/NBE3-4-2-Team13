package com.app.backend.domain.post.repository.postAttachment;

import java.util.List;

public interface PostAttachmentRepositoryCustom {

    void deleteByIdList(List<Long> idList);

    void deleteByPostId(Long postId);
}
