package com.app.backend.domain.member.repository;

import com.app.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByIdAndDisabled(Long in, boolean disabled);

    Optional<Member> findByUsernameAndDisabled(String username, boolean disabled);

    Optional<Member> findByNicknameAndDisabled(String nickname, boolean disabled);

    Optional<Member> findByOauthProviderId(String oauthProviderId);

    int deleteByDisabledIsTrueAndModifiedAtLessThan(LocalDateTime modifiedAt);
}
