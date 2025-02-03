package com.app.backend.domain.group.entity;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_group_memberships")
@IdClass(GroupMembershipId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMembership extends BaseEntity {

    @Id
    @Column(name = "member_id", insertable = false, updatable = false)
    private Long memberId;  //회원 ID

    @Id
    @Column(name = "group_id", insertable = false, updatable = false)
    private Long groupId;   //모임 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;  //회원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private Group group;    //모임

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole groupRole;    //모임 내 회원의 권한: LEADER, PARTICIPANT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;    //모임 내 회원의 상태: PENDING, APPROVED, REJECTED, LEAVE

    @Builder
    public GroupMembership(@NotNull final Member member,
                           @NotNull final Group group,
                           @NotNull final GroupRole groupRole) {
        setRelationshipWithMember(member);
        setRelationshipWithGroup(group);
        this.groupRole = groupRole;
        this.status = groupRole == GroupRole.LEADER ? MembershipStatus.APPROVED : MembershipStatus.PENDING;
    }

    //============================== 연관관계 메서드 ==============================//

    private void setRelationshipWithMember(@NotNull final Member member) {
        memberId = member.getId();
        this.member = member;
        //TODO: 필요 시, 회원(Member)과의 연관관계 설정 추가
    }

    private void setRelationshipWithGroup(@NotNull final Group group) {
        groupId = group.getId();
        this.group = group;
        group.getMembers().add(this);
    }

    //============================== 모임 멤버십(GroupMembership) 수정 메서드 ==============================//

    /**
     * 모임 내 회원 권한 수정
     *
     * @param newGroupRole - 새로운 모임 내 권한
     * @return this
     */
    public GroupMembership modifyGroupRole(@NotNull final GroupRole newGroupRole) {
        if (groupRole != newGroupRole)
            groupRole = newGroupRole;
        return this;
    }

    /**
     * 모임 내 회원 상태 수정
     *
     * @param newStatus - 새로운 멤버십 상태
     * @return this
     */
    public GroupMembership modifyStatus(@NotNull final MembershipStatus newStatus) {
        if (status != newStatus)
            status = newStatus;
        return this;
    }

}
