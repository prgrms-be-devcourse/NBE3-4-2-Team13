package com.app.backend.domain.post.dto.resp;

import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.global.config.FileConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

public class PostAttachmentRespDto {

    @Getter
    public static class GetPostImageDto {
        private final Long attachmentId;
        private final String fileName;
        private final FileType fileType;
        private final String filePath;
        private final Long fileSize;

        public GetPostImageDto(final PostAttachment postAttachment, String basePath) {
            this.attachmentId = postAttachment.getId();
            this.fileName = postAttachment.getOriginalFileName();
            this.fileType = postAttachment.getFileType();
            this.filePath = basePath + "/" + postAttachment.getStoreFilePath();
            this.fileSize = postAttachment.getFileSize();
        }
    }

    @Getter
    public static class GetPostDocumentDto {
        private final Long attachmentId;
        private final String fileName;
        private final FileType fileType;
        private final Long fileSize;

        public GetPostDocumentDto(final PostAttachment postAttachment) {
            this.attachmentId = postAttachment.getId();
            this.fileName = postAttachment.getOriginalFileName();
            this.fileType = postAttachment.getFileType();
            this.fileSize = postAttachment.getFileSize();
        }
    }

}
