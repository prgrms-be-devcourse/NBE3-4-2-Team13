package com.app.backend.domain.attachment.util;

import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;


public class FileUtil {

    // 파일 이름 생성
    public static String generateFileName(final Path dateDir, final String ext) throws IOException {
        String currentDate = dateDir.getFileName().toString(); // yyyyMMdd 형식

        String shortUUID = UUID.randomUUID().toString().substring(0, 8);


        // yyyyMMdd_UUID 형식의 파일 이름 생성
        return currentDate + "_" + shortUUID + "." + ext;
    }

    // 파일 이름
    public static String getFileName(final String filePath) {
        int lastIndex = filePath.lastIndexOf("/");

        if (lastIndex == -1) {
            throw new FileException(FileErrorCode.FILE_NOT_FOUND);
        }

        return filePath.substring(lastIndex + 1);
    }

    // 파일 확장자
    public static String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');

        if (lastIndex == -1) {
            throw new FileException(FileErrorCode.INVALID_FILE_EXTENSION);
        }

        return fileName.substring(lastIndex + 1).toLowerCase();
    }

    // 파일 타입
    public static FileType getFileType(final String fileName) {

        String fileType = getExtension(fileName);

        switch (fileType) {
            case "jpeg", "jpg", "png", "gif" -> {
                return FileType.IMAGE;
            }
            case "pdf", "doc", "docx" -> {
                return FileType.DOCUMENT;
            }
            case "mp4", "avi", "mkv" -> {
                return FileType.VIDEO;
            }
            default -> {
                throw new FileException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
            }
        }
    }
}
