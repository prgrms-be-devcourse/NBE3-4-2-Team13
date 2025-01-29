package com.app.backend.domain.post.dto.req;

import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PostReqDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyPost {

        @NotNull
        private String title;
        @NotNull
        private String content;
        @NotNull
        private PostStatus postStatus;

        private Long oldFileSize;

        private List<Long> remainIdList;

        private List<Long> removeIdList;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavePost {

        @NotNull
        private String title;
        @NotNull
        private String content;
        @NotNull
        private PostStatus postStatus;
        @NotNull
        private Long groupId;

        public Post toEntity(Group group, Member member) {
            return Post
                    .builder()
                    .title(this.title)
                    .content(this.content)
                    .postStatus(this.postStatus)
                    .group(group)
                    .member(member)
                    .build();
        }

    }
}
