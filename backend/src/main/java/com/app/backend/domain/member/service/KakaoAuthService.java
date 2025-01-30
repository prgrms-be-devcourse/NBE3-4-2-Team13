package com.app.backend.domain.member.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.app.backend.domain.member.dto.kakao.KakaoUserInfo;
import com.app.backend.domain.member.dto.kakao.TokenDto;
import com.app.backend.domain.member.entity.Member;
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

	public TokenDto kakaoLogin(String code) {
		// 1. 인가코드로 액세스 토큰 요청
		String kakaoAccessToken = getKakaoAccessToken(code);
		
		// 2. 액세스 토큰으로 카카오 API 호출
		KakaoUserInfo userInfo = getKakaoUserInfo(kakaoAccessToken);

		// 3. 회원가입 & 로그인 처리
		Member member = saveOrUpdate(userInfo);

		// 4. JWT 토큰 발급
		String accessToken = jwtProvider.generateAccessToken(member);
		String refreshToken = jwtProvider.generateRefreshToken();
		
		member.updateRefreshToken(refreshToken);
		memberRepository.save(member);

		return new TokenDto(accessToken, refreshToken);
	}

	private String getKakaoAccessToken(String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", clientId);
		params.add("redirect_uri", redirectUri);
		params.add("code", code);
		params.add("client_secret", clientSecret);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

		ResponseEntity<Map> response = restTemplate.postForEntity(
			"https://kauth.kakao.com/oauth/token",
			request,
			Map.class
		);

		return (String) response.getBody().get("access_token");
	}

	// 필수 동의항목 설정 필요
	private KakaoUserInfo getKakaoUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		
		HttpEntity<String> request = new HttpEntity<>(headers);
		
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
