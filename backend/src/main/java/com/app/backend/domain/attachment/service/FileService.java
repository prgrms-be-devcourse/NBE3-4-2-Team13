package com.app.backend.domain.attachment.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    String saveFile(MultipartFile file);

    void deleteFile(String storedFilename);

    void deleteFiles(List<String> filePaths);
}
