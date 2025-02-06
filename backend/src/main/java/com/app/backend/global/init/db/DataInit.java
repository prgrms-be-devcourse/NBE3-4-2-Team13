package com.app.backend.global.init.db;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.Member.Provider;
import com.app.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataInit {

    private final MemberRepository memberRepository;
    private final PasswordEncoder  passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void init() {
        if (memberRepository.findAll().stream().noneMatch(m -> m.getRole().equals("admin"))) {
            Member member = Member.builder()
                                  .username("admin")
                                  .password(passwordEncoder.encode("admin"))
                                  .nickname("admin")
                                  .role("ADMIN")
                                  .provider(Provider.LOCAL)
                                  .build();
            memberRepository.save(member);
        }
    }

}
