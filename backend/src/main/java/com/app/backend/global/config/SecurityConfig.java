package com.app.backend.global.config;

import com.app.backend.domain.member.jwt.JwtAuthenticationFilter;
import com.app.backend.domain.member.jwt.JwtProvider;
import com.app.backend.domain.member.oauth.OAuth2SuccessHandler;
import com.app.backend.domain.member.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtProvider             jwtProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler    oAuth2SuccessHandler;

    private final String[] allowedOrigins = {
            "http://localhost:3000", // React
            "http://localhost:5173", // Vite
            "http://localhost:8080" // Spring Boot
    };

    @Bean // 비밀번호 암호화
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        // Swagger UI 관련
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // H2 콘솔
                        .requestMatchers("/h2-console/**").permitAll()
                        // OAuth2 관련
                        .requestMatchers(
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/*"
                        ).permitAll()
                        // API 엔드포인트
                        .requestMatchers(
                                "/api/v1/members/**",
                                "/images/**",
                                "/ws/**",
                                "/api/v1/proxy/kakao/**",
                                "/api/v1/notifications/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
										"style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; object-src " +
										"'none'; base-uri 'self'; connect-src 'self' https://kauth.kakao.com " +
										"https://kapi.kakao.com; frame-ancestors 'self'; form-action 'self'; " +
										"block-all-mixed-content")))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorizationEndpointConfig -> authorizationEndpointConfig
                                .baseUri("/oauth2/authorization/kakao")) // 카카오 로그인 시작점
                        .redirectionEndpoint(redirectionEndpointConfig -> redirectionEndpointConfig
                                .baseUri("/login/oauth2/code/*")) // 카카오 인증 후 백엔드 콜백
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(new OAuth2AuthenticationFailureHandler()))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 허용할 헤더 설정
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"));
        // 인증 정보 허용
        configuration.setAllowCredentials(true);
        // pre-flight 요청 캐시 시간
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

// OAuth2 실패 핸들러를 별도 클래스로 분리
@Component
class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorMessage = getErrorMessage(exception);
        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/login")
                .queryParam("error", errorMessage)
                .build().toUriString();
        response.sendRedirect(targetUrl);
    }

    private String getErrorMessage(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
            return switch (error.getErrorCode()) {
                case "invalid_token" -> "유효하지 않은 토큰입니다.";
                case "invalid_request" -> "잘못된 요청입니다.";
                // ... 나머지 케이스들
                default -> "로그인 처리 중 오류가 발생했습니다: " + error.getDescription();
            };
        }
        return "로그인 처리 중 오류가 발생했습니다.";
    }
}