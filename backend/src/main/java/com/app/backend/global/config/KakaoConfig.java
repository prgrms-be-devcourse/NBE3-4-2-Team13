package com.app.backend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "kakao")
@Getter
@Setter
public class KakaoConfig {
	private String id;
	private String secret;
	private String redirectUri;
}
