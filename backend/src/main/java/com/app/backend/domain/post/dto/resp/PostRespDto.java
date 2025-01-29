package com.app.backend.domain.post.dto.resp;

import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.domain.post.entity.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class PostRespDto {

    @Getter
    @AllArgsConstructor
    public static class PostIdDto {
        private final Long postId;
    }

    @Getter
    public static class GetPostDto {

        private final Long postId;
        private final String title;
        private final String content;
        private final PostStatus postStatus;
        private final Long memberId;
        private final Long groupId;
        private final List<PostAttachmentRespDto.GetPostAttachment> attachments;

        public GetPostDto(final Post post, final List<PostAttachmentRespDto.GetPostAttachment> attachments) {
            this.postId = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.postStatus = post.getPostStatus();
            this.memberId = post.getMember().getId();
            this.groupId = post.getGroup().getId();
            this.attachments = attachments;
        }

    }

}
