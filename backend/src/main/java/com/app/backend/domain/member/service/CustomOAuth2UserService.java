package com.app.backend.domain.member.service;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // 카카오 계정 정보 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        
        String nickname = (String) profile.get("nickname");
        String oauthId = String.valueOf(attributes.get("id"));

        Member member = memberRepository.findByOauthProviderId(oauthId)
            .orElseGet(() -> createMember(oauthId, nickname));

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority(member.getRole())),
            attributes,
            userNameAttributeName
        );
    }

    private Member createMember(String oauthId, String nickname) {
        Member member = Member.builder()
            .username(oauthId)
            .nickname(nickname)
            .provider(Member.Provider.KAKAO)
            .oauthProviderId(oauthId)
            .role("USER")
            .disabled(false)
            .build();

        return memberRepository.save(member);
    }
} 