package com.app.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileConfig implements WebMvcConfigurer {
    @Value("${spring.file.base-dir}")
    private String BASE_DIR;

    @Value("${spring.file.img-dir}")
    private String IMAGE_DIR;

    public String getBASE_DIR() {
        return BASE_DIR;
    }

    public String getIMAGE_DIR() {
        return IMAGE_DIR;
    }

    // 정적 리소스 방식
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file://"+ BASE_DIR + "/");
    }
}
