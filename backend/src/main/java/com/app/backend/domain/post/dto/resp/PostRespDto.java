package com.app.backend.domain.post.dto.resp;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.global.util.AppUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class PostRespDto {

    @Getter
    @AllArgsConstructor
    public static class GetPostIdDto {
        private final Long postId;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPostDto {

        private final Long postId;
        private final String title;
        private final String content;
        private final PostStatus postStatus;
        private final String nickName;
        private final Long memberId;
        private final Long groupId;
        private final String createdAt;
        private final String modifiedAt;
        private final List<PostAttachmentRespDto.GetPostImageDto> images;
        private final List<PostAttachmentRespDto.GetPostDocumentDto> documents;
    }

    public static GetPostDto toGetPost(final Post post,
                                       final Member member,
                                       final List<PostAttachmentRespDto.GetPostImageDto> images,
                                       final List<PostAttachmentRespDto.GetPostDocumentDto> documents)
    {
        return GetPostDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .postStatus(post.getPostStatus())
                .nickName(member.getNickname())
                .memberId(member.getId())
                .groupId(post.getGroupId())
                .createdAt(AppUtil.localDateTimeToString(post.getCreatedAt()))
                .modifiedAt(AppUtil.localDateTimeToString(post.getModifiedAt()))
                .images(images)
                .documents(documents)
                .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPostListDto {
        private final Long postId;
        private final String title;
        private final PostStatus postStatus;
        private final Long memberId;
        private final String nickName;
        private final String createdAt;
        private final Long todayViewCount;
    }

    public static GetPostListDto toGetPostList(final Post post){
        return GetPostListDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .postStatus(post.getPostStatus())
                .memberId(post.getMemberId())
                .nickName(post.getNickName())
                .createdAt(AppUtil.localDateTimeToString(post.getCreatedAt()))
                .todayViewCount(post.getTodayViewCount())
                .build();
    }
}
