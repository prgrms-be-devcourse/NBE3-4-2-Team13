package com.app.backend.domain.member.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.app.backend.domain.member.dto.kakao.KakaoUserInfo;
import com.app.backend.domain.member.dto.kakao.TokenDto;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.exception.MemberErrorCode;
import com.app.backend.domain.member.exception.MemberException;
import com.app.backend.domain.member.jwt.JwtProvider;
import com.app.backend.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthService {
	private final RestTemplate restTemplate;
	private final JwtProvider jwtProvider;
	private final MemberRepository memberRepository;

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String clientId;
	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String clientSecret;
	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String redirectUri;

	@Transactional
	public TokenDto kakaoLogin(String code) {
		try {
			// 1. 인가코드로 액세스 토큰 요청
			String kakaoAccessToken = getKakaoAccessToken(code);

			// 2. 액세스 토큰으로 카카오 API 호출
			KakaoUserInfo userInfo = getKakaoUserInfo(kakaoAccessToken);

			// 3. 회원가입 & 로그인 처리
			Member member = saveOrUpdate(userInfo);

			// 4. JWT 토큰 발급
			String accessToken = jwtProvider.generateAccessToken(member);
			String refreshToken = jwtProvider.generateRefreshToken();

			memberRepository.save(member);

			return new TokenDto(userInfo.id() ,accessToken, refreshToken, "USER");
		} catch (Exception e){
			log.error("카카오 로그인 처리 중 오류: {}", e.getMessage());
			if (e.getMessage().contains("authorization code not found")) {
				// 이미 사용된 코드인 경우 특별한 처리
				log.warn("이미 사용된 인증 코드입니다.");
			}
			throw e;
		}
	}

	private String getKakaoAccessToken(String code) {
		try {
			String tokenUri = UriComponentsBuilder
				.fromUriString("https://kauth.kakao.com/oauth/token")
				.queryParam("grant_type", "authorization_code")
				.queryParam("client_id", clientId)
				.queryParam("redirect_uri", redirectUri)
				.queryParam("code", code)
				.queryParam("client_secret", clientSecret)
				.build()
				.toString();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			HttpEntity<?> request = new HttpEntity<>(headers);

			ResponseEntity<Map> response = restTemplate.exchange(
				tokenUri,
				HttpMethod.GET,
				request,
				Map.class
			);

			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
				return (String) response.getBody().get("access_token");

			throw new MemberException(MemberErrorCode.MEMBER_FAILED_TO_KAKAO_TOKEN);
		} catch (Exception e) {
			log.error("카카오 토큰 요청 중 오류 발생: {}", e.getMessage());
			throw new RuntimeException("카카오 토큰 요청 실패", e);
		}
	}

	// 필수 동의항목 설정 필요
	public KakaoUserInfo getKakaoUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		
		HttpEntity<String> request = new HttpEntity<>(headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(
				"https://kapi.kakao.com/v2/user/me", // 카카오 사용자 정보 조회 api
				HttpMethod.GET,
				request,
				Map.class
			);

			Map<String, Object> body = response.getBody();
			Map<String, Object> properties = (Map<String, Object>) body.get("properties");

			return new KakaoUserInfo(
				String.valueOf(body.get("id")),
				(String) properties.get("nickname")
			);
		} catch (Exception e) {
			log.error("카카오 API 호출 실패: {}", e.getMessage());
			throw new MemberException(MemberErrorCode.MEMBER_FAILED_TO_KAKAO_AUTH);
		}
	}

	private Member saveOrUpdate(KakaoUserInfo userInfo) {
		return memberRepository.findByOauthProviderId(userInfo.id())
			.orElseGet(() -> memberRepository.save(Member.builder()
				.username(userInfo.id())
				.password("")
				.nickname(userInfo.nickname())
				.provider(Member.Provider.KAKAO)
				.oauthProviderId(userInfo.id())
				.role("ROLE_USER")
				.disabled(false)
				.build()));
	}
}
