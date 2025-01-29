package com.app.backend.domain.attachment.entity;

import com.app.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Attachment extends BaseEntity {

    @Column(name = "original_filename", nullable = false)
    private String originalFileName;

    @Column(name = "store_filename", nullable = false)
    private String storeFileName;

    @Column(name = "store_file_path", nullable = false)
    private String storeFilePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_type")
    @Enumerated(EnumType.STRING)
    private FileType fileType;

}
