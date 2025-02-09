package com.app.backend.global.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.util.UriComponentsBuilder;

import com.app.backend.domain.member.jwt.JwtAuthenticationFilter;
import com.app.backend.domain.member.jwt.JwtProvider;
import com.app.backend.domain.member.oauth.OAuth2SuccessHandler;
import com.app.backend.domain.member.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

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
                        .requestMatchers(
                                "/images/**",
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/*",
                                "/api/v1/members/kakao/**",
                                "/api/v1/members/login",
                                "/api/v1/members/join",
                                "/api/v1/members/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/ws/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; object-src 'none'; base-uri 'self'; connect-src 'self' https://kauth.kakao.com https://kapi.kakao.com; frame-ancestors 'self'; form-action 'self'; block-all-mixed-content")))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorizationEndpointConfig -> authorizationEndpointConfig
                                .baseUri("/oauth2/authorization/kakao")) // 카카오 로그인 시작점
                        .redirectionEndpoint(redirectionEndpointConfig -> redirectionEndpointConfig
                                .baseUri("/login/oauth2/code/*")) // 카카오 인증 후 백엔드 콜백
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            String errorMessage = "";
                            if (exception instanceof OAuth2AuthenticationException) {
                                OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
                                errorMessage = switch (error.getErrorCode()) {
                                    case "invalid_token" -> "유효하지 않은 토큰입니다.";
                                    case "invalid_request" -> "잘못된 요청입니다.";
                                    case "invalid_client" -> "클라이언트 인증에 실패했습니다.";
                                    case "invalid_grant" -> "유효하지 않은 인증입니다.";
                                    case "unauthorized_client" -> "인증되지 않은 클라이언트입니다.";
                                    case "unsupported_grant_type" -> "지원하지 않는 인증 방식입니다.";
                                    case "invalid_scope" -> "유효하지 않은 스코프입니다.";
                                    case "access_denied" -> "접근이 거부되었습니다.";
                                    case "user_load_error" -> "사용자 정보를 가져오는데 실패했습니다.";
                                    default -> "로그인 처리 중 오류가 발생했습니다: " + error.getDescription();
                                };
                            } else {
                                errorMessage = "로그인 처리 중 오류가 발생했습니다.";
                            }
                            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/login")
                                    .queryParam("error", exception.getMessage())
                                    .build().toUriString();
                            response.sendRedirect(targetUrl);
                        }))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 허용할 헤더 설정
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
