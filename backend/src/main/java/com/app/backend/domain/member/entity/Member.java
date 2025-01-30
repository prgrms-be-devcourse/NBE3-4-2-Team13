package com.app.backend.domain.member.entity;

import com.app.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "tbl_members")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", length = 255)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", length = 255)
    private Provider provider;

    @Column(name = "oauth_provider_id", length = 255)
    private String oauthProviderId;

    @Column(name = "role", length = 255)
    private String role;

    @Column(nullable = false)
    private boolean disabled;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Override
    public void activate() {
        super.deactivate();
    }

    public enum Provider {
        LOCAL, KAKAO
        // 필요시 NAVER, GOOGLE 등 추가
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
