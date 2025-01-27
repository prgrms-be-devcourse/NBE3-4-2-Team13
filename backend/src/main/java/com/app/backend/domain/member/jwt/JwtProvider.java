package com.app.backend.domain.member.jwt;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.member.repository.MemberRepository;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // 1일

    private final MemberRepository memberRepository;

    // access token 생성
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .claim("name", username)
                .claim("id", getMemberId(username))
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
        // 1. 토큰에서 username(subject) 추출
        String username = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        // 2. MemberDetails 객체 생성
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        MemberDetails memberDetails = MemberDetails.of(member);

        // 3. Authentication 객체 생성 및 반환
        return new UsernamePasswordAuthenticationToken(
                memberDetails,
                "",
                memberDetails.getAuthorities()
        );
    }

    private Long getMemberId(String username) {
        return memberRepository.findByUsername(username)
                .map(Member::getId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
