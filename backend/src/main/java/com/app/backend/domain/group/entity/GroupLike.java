package com.app.backend.domain.group.entity;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "tbl_groupLikes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "member_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupLike extends BaseEntity {

    @Id
    @Column(name = "group_like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Builder
    private GroupLike(Group group, Member member) {
        this.group = group;
        this.member = member;
    }
}