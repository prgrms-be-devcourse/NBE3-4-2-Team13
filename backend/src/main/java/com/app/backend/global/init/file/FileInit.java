package com.app.backend.global.init.file;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileInit {

    @Value("${spring.file.base-dir}")
    private String BASE_DIR;

    @PostConstruct
    public void init() {
        File uploadDir = new File(BASE_DIR);

        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("Upload directory created at: " + uploadDir.getAbsolutePath());
            }
        }

    }
}