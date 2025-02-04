package com.app.backend.domain.attachment.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class FileServiceTest {

    @Value("${spring.file.base-dir}")
    private String BASE_DIR;

    @Autowired
    private FileService fileService;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file",
                "test1.jpg",
                "image/jpeg",
                "test".getBytes()
        );
    }

    @AfterEach
    void afterEach() throws IOException {
        Path dir = Paths.get(BASE_DIR);
        if (Files.exists(dir)) {
            FileSystemUtils.deleteRecursively(dir);
        }
    }

    @Test
    @DisplayName("Success : 파일 업로드")
    public void testSave() {

        String filePath = BASE_DIR + "/" + fileService.saveFile(mockFile);

        Assertions.assertThat(filePath).isNotNull();
        Assertions.assertThat(Files.exists(Paths.get(filePath))).isTrue();
    }

    @Test
    @DisplayName("Success : 파일 삭제")
    public void testDelete() {

        String filePath = fileService.saveFile(mockFile);

        Assertions.assertThat(filePath).isNotNull();

        fileService.deleteFile(filePath);

        Assertions.assertThat(Files.exists(Paths.get(filePath))).isFalse();
    }

}
