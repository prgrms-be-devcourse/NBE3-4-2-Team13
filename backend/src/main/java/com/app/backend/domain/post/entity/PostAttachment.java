package com.app.backend.domain.post.entity;

import com.app.backend.domain.attachment.entity.Attachment;
import com.app.backend.domain.attachment.entity.FileType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "tbl_post_attachments")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAttachment extends Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    public PostAttachment(String originalFileName, String storeFileName, String storeFilePath, Long fileSize, String contentType, FileType fileType, Long postId) {
        super(originalFileName, storeFileName, storeFilePath, fileSize, contentType, fileType);
        this.postId = postId;
    }

    public void delete() {
        if (!this.getDisabled()) {
            deactivate();
        }
    }
}
