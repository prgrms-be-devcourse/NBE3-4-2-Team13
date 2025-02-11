package com.app.backend.domain.member.service;

import com.app.backend.domain.group.dto.response.GroupMembershipResponse;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.member.dto.request.MemberLoginRequestDto;
import com.app.backend.domain.member.dto.request.MemberModifyRequestDto;
import com.app.backend.domain.member.dto.response.MemberJoinResponseDto;
import com.app.backend.domain.member.dto.response.MemberLoginResponseDto;
import com.app.backend.domain.member.dto.response.MemberModifyResponseDto;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.exception.MemberErrorCode;
import com.app.backend.domain.member.exception.MemberException;
import com.app.backend.domain.member.jwt.JwtProvider;
import com.app.backend.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {
    private final MemberRepository          memberRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final PasswordEncoder           passwordEncoder;
    private final JwtProvider               jwtProvider;
    private final boolean                   disabled = false;

    @Transactional
    public MemberJoinResponseDto createMember(String username, String password, String nickname) {
        memberRepository.findByUsernameAndDisabled(username, disabled)
                        .ifPresent(a -> {
                            throw new MemberException(MemberErrorCode.MEMBER_USERNAME_EXISTS);
                        });
        memberRepository.findByNicknameAndDisabled(nickname, disabled)
                        .ifPresent(a -> {
                            throw new MemberException(MemberErrorCode.MEMBER_NICKNAME_EXISTS);
                        });

        Member member = Member.builder()
                              .username(username)
                              .password(passwordEncoder.encode(password))
                              .nickname(nickname)
                              .role("ROLE_ADMIN")
                              .disabled(false)
                              .build();

        Member savedMember = memberRepository.save(member);

        // Entity -> Response DTO 변환
        return MemberJoinResponseDto.from(savedMember);
    }

    public MemberLoginResponseDto login(MemberLoginRequestDto request) {
        Member member = memberRepository.findByUsernameAndDisabled(request.username(), disabled)
                                        .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword()))
            throw new MemberException(MemberErrorCode.MEMBER_PASSWORD_NOT_MATCH);

        // 토큰 생성
        String accessToken  = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken();

        memberRepository.save(member);

        // 응답
        return MemberLoginResponseDto.of(member, accessToken, refreshToken);
    }

    @Transactional
    public void logout(String token) {
        try {
            Member member = getCurrentMember(token);
            memberRepository.save(member);
        } catch (Exception e) {
            throw new MemberException(MemberErrorCode.MEMBER_FAILED_LOGOUT);
        }
    }

    public Member getCurrentMember(String accessToken) {
        return Optional.ofNullable(accessToken)
                       .map(t -> t.startsWith("Bearer ") ? t.substring(7) : t)
                       .filter(jwtProvider::validateToken)
                       .map(validateToken -> {
                           Long memberId = jwtProvider.getMemberId(validateToken);
                           return this.memberRepository.findByIdAndDisabled(memberId, false)
                                                       .orElseThrow(() -> new MemberException(
                                                               MemberErrorCode.MEMBER_NOT_FOUND));
                       })
                       .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_UNVALID_TOKEN));  // 토큰 검증
    }

    @Transactional
    public MemberModifyResponseDto modifyMember(Member member, MemberModifyRequestDto request) {
        Member modifiedMember = Member.builder()
                                      .id(member.getId())
                                      .username(member.getUsername())
                                      .password(request.password() != null ?
                                                passwordEncoder.encode(request.password()) : member.getPassword())
                                      .nickname(member.getNickname())
                                      .role(member.getRole())
                                      .disabled(member.isDisabled())
                                      .build();

        Member savedMember = Optional.of(
                memberRepository.save(modifiedMember)
        ).orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_FAILED_TO_MODIFY));

        return MemberModifyResponseDto.of(savedMember);
    }

    public Optional<List<Member>> findAllMembers(String token) {
        return Optional.ofNullable(token)
                       .map(t -> t.startsWith("Bearer ") ? t.substring(7) : t)
                       .filter(jwtProvider::validateToken)
                       .filter(validateToken -> {
                           String role = jwtProvider.getRole(validateToken);
                           if (!role.contains("ADMIN"))
                               throw new MemberException(MemberErrorCode.MEMBER_NO_ADMIN_PERMISSION);
                           return true;
                       })
                       .map(validateToken -> memberRepository.findAllByOrderByIdDesc());
    }

    @Transactional
    public void deleteMember(String token) {
        Member member = getCurrentMember(token);
        member = Member.builder()
                       .id(member.getId())
                       .username(member.getUsername())
                       .password(member.getPassword())
                       .nickname(member.getNickname())
                       .provider(member.getProvider())
                       .oauthProviderId(member.getOauthProviderId())
                       .role(member.getRole())
                       .disabled(true)
                       .build();

        memberRepository.save(member);
    }

    @Transactional
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void cleanupDisabledMembers() {
        log.info("비활성화된 회원 정보 삭제 작업 시작");
        LocalDateTime cutoffDate   = LocalDateTime.now().minusSeconds(30);
        int           deletedCount = memberRepository.deleteByDisabledIsTrueAndModifiedAtLessThan(cutoffDate);
        log.info("삭제된 회원 수: {}", deletedCount);
    }

    /*
        public List<Group> getMyGroup(String token) {
            return Optional.ofNullable(token)
                .map(t -> t.startsWith("Bearer ") ? t.substring(7) : t)
                .filter(jwtProvider::validateToken)
                .map(validateToken -> {
                    Long id = jwtProvider.getMemberId(validateToken);
                    return groupMembershipRepository.findAllByMemberIdAndDisabled(id, false)
                        .stream()
                        .map(GroupMembership::getGroup)
                        .toList();
                })
                .orElse(List.of());  // 토큰이 없거나 유효하지 않은 경우 빈 리스트 반환
        }
    */
    public List<GroupMembershipResponse.Detail> getMyGroup(String token) {
        return Optional.ofNullable(token)
                       .map(t -> t.startsWith("Bearer ") ? t.substring(7) : t)
                       .filter(jwtProvider::validateToken)
                       .map(validateToken -> {
                           Long id = jwtProvider.getMemberId(validateToken);
                           return groupMembershipRepository.findAllByMemberIdAndDisabled(id, false)
                                                           .stream()
                                                           .map(GroupMembershipResponse::toDetail)
                                                           .toList();
                       })
                       .orElse(List.of());  // 토큰이 없거나 유효하지 않은 경우 빈 리스트 반환
    }
}
