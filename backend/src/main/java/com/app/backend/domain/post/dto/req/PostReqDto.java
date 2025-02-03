package com.app.backend.domain.post.dto.req;

import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class PostReqDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchPostDto {
        @Positive
        private Long groupId;

        @JsonProperty(defaultValue = "")
        private String search;

        @JsonProperty(defaultValue = "All")
        private PostStatus postStatus;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyPostDto {
        @Positive
        private Long groupId;
        @NotNull
        private String title;
        @NotNull
        private String content;
        @NotNull
        private PostStatus postStatus;

        @JsonProperty(defaultValue = "0")
        private Long oldFileSize;

        private List<Long> remainIdList;

        private List<Long> removeIdList;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavePostDto {

        @NotNull
        private String title;
        @NotNull
        private String content;
        @NotNull
        private PostStatus postStatus;
        @Positive
        private Long groupId;

        public Post toEntity(Long memberId) {
            return Post
                    .builder()
                    .title(this.title)
                    .content(this.content)
                    .postStatus(this.postStatus)
                    .groupId(this.groupId)
                    .memberId(memberId)
                    .build();
        }
    }
}
