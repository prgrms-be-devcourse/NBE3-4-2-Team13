package com.app.backend.domain.member.jwt;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {
	private final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
	private final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // 1일

	// access token 생성
	public String generateAccessToken(Member member) {
		return Jwts.builder()
			.claim("name", member.getUsername())
			.claim("id", member.getId())
			.claim("role", member.getRole())
			.expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
			.signWith(getSigningKey())
			.compact();
	}

	// refresh token 생성
	public String generateRefreshToken() {
		return Jwts.builder()
			.expiration(Date.from(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRE_TIME)))
			.signWith(getSigningKey())
			.compact();
	}

	// 토큰 유효성 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private SecretKey getSigningKey() {
		return Jwts.SIG     // JJWT 의 서명 기능 사용
			.HS256      // HMAC(대칭키 암호화 알고리즘. 256비트 보안 강도) SHA-256 알고리즘 사용
			.key()       // 키 생성
			.build();  // 자동으로 안전한 키 생성
	}

	public Authentication getAuthentication(String token) {
		// 토큰에서 모든 필요한 정보를 추출
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

        // 토큰의 claim에서 필요한 정보를 가져옴
		Member member = Member.builder()
			.id(claims.get("id", Long.class))
			.username(claims.get("name", String.class))
			.role(claims.get("role", String.class))
			.build();
		MemberDetails memberDetails = MemberDetails.of(member);

		// Authentication 객체 생성 및 반환
		return new UsernamePasswordAuthenticationToken(
			memberDetails,
			"",
			memberDetails.getAuthorities()
		);
	}
}
