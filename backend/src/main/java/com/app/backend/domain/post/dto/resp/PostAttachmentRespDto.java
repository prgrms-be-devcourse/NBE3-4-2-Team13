package com.app.backend.domain.post.dto.resp;

import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.post.entity.PostAttachment;
import lombok.Getter;

public class PostAttachmentRespDto {

    @Getter
    public static class GetPostAttachment{
        private final Long attachmentId;
        private final String fileName;
        private final FileType fileType;
        private final String filePath;
        private final Long fileSize;

        public GetPostAttachment(final PostAttachment postAttachment) {
            this.attachmentId = postAttachment.getId();
            this.fileName = postAttachment.getOriginalFileName();
            this.fileType = postAttachment.getFileType();
            this.filePath = postAttachment.getStoreFilePath();
            this.fileSize = postAttachment.getFileSize();
        }
    }

}
