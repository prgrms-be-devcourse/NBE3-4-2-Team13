package com.app.backend.domain.attachment.dto.resp;

import com.app.backend.domain.post.entity.PostAttachment;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

public class FileRespDto {

    @Getter
    @Builder
    public static class downloadDto{
        private Resource resource;
        private PostAttachment attachment;
    }

}
