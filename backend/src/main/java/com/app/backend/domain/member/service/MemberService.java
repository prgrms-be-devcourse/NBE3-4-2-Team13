package com.app.backend.domain.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.member.dto.request.MemberLoginRequestDto;
import com.app.backend.domain.member.dto.response.MemberJoinResponseDto;
import com.app.backend.domain.member.dto.response.MemberLoginResponseDto;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.jwt.JwtProvider;
import com.app.backend.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public MemberJoinResponseDto createMember(String username, String password, String nickname) {
        memberRepository.findByUsername("testID")
                .ifPresent(a -> {
                    throw new ServiceException("409-1", "이미 존재하는 username 입니다.");
                });
        memberRepository.findByNickname("김호남")
                .ifPresent(a->{
                    throw new ServiceException("409-2", "이미 존재하는 닉네임입니다");
                });

        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .role("ADMIN")
                .disabled(false)
                .build();

        Member savedMember = memberRepository.save(member);

        // Entity -> Response DTO 변환
        return MemberJoinResponseDto.from(savedMember);
    }

    public MemberLoginResponseDto login(MemberLoginRequestDto request) {
        // 사용자 찾기
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다."));

        // 비밀번호 확인
        if(!passwordEncoder.matches(request.password(), member.getPassword()))
            throw new IllegalArgumentException("잘못된 비밀번호입니다");

        // 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken();

        // refresh token 저장
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        // 응답
        return MemberLoginResponseDto.of(member, accessToken, refreshToken);
    }

}
