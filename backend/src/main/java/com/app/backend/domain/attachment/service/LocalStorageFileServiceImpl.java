package com.app.backend.domain.attachment.service;

import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import com.app.backend.domain.attachment.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class LocalStorageFileServiceImpl implements FileService {

    @Value("${spring.file.base-dir}")
    private String BASE_DIR;

    @Override
    public String saveFile(MultipartFile file) {
        try {
            // 현재 날짜로 폴더 경로 생성
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path dateDir = Paths.get(BASE_DIR, currentDate);

            // 디렉토리가 존재하지 않으면 생성
            if (!Files.exists(dateDir)) {
                Files.createDirectories(dateDir);
            }

            String originalFileName = file.getOriginalFilename();

            String ext = FileUtil.getExtension(originalFileName != null ? originalFileName : "");

            // 저장 이름 생성 (yyyyMMdd_uuid 형식)
            String storedFilename = FileUtil.generateFileName(dateDir, ext);

            // 파일 저장 경로 생성
            Path filePath = dateDir.resolve(storedFilename);

            // 파일 저장
            file.transferTo(filePath.toFile());

            return String.format("%s/%s/%s", BASE_DIR, currentDate, storedFilename);
        } catch (IOException e) {
            throw new FileException(FileErrorCode.FAILED_FILE_SAVE);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete(); // 물리적 파일 삭제
        }
    }
}
