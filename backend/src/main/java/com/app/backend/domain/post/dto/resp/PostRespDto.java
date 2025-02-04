package com.app.backend.domain.post.dto.resp;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.global.util.AppUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class PostRespDto {

    @Getter
    @AllArgsConstructor
    public static class GetPostIdDto {
        private final Long postId;
    }

    @Getter
    public static class GetPostDto {

        private final Long postId;
        private final String title;
        private final String content;
        private final PostStatus postStatus;
        private final String nickName;
        private final Long groupId;
        private final String createdAt;
        private final String modifiedAt;
        private final List<PostAttachmentRespDto.GetPostImageDto> images;
        private final List<PostAttachmentRespDto.GetPostDocumentDto> documents;

        public GetPostDto(
                final Post post,
                final Member member,
                final List<PostAttachmentRespDto.GetPostImageDto> images,
                final List<PostAttachmentRespDto.GetPostDocumentDto> documents
        ) {
            this.postId = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.postStatus = post.getPostStatus();
            this.nickName = member.getNickname();
            this.groupId = post.getGroupId();
            this.createdAt = AppUtil.localDateTimeToString(post.getCreatedAt());
            this.modifiedAt = AppUtil.localDateTimeToString(post.getModifiedAt());
            this.images = images;
            this.documents = documents;
        }
    }

    @Getter
    public static class GetPostListDto {
        private final Long postId;
        private final String title;
        private final String content;
        private final PostStatus postStatus;
        private final Long memberId;
        private final String createdAt;

        public GetPostListDto(final Post post) {
            this.postId = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.postStatus = post.getPostStatus();
            this.memberId = post.getMemberId();
            this.createdAt = AppUtil.localDateTimeToString(post.getCreatedAt());
        }
    }
}
