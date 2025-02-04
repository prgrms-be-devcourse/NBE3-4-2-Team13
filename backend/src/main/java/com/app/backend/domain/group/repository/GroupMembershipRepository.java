package com.app.backend.domain.group.repository;

import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, GroupMembershipId>,
                                                   GroupMembershipRepositoryCustom {

    Optional<GroupMembership> findByGroupIdAndMemberId(Long groupId, Long memberId);

    Optional<GroupMembership> findByGroupIdAndMemberIdAndDisabled(Long groupId, Long memberId, Boolean disabled);

    List<GroupMembership> findAllByGroupId(Long groupId);

    List<GroupMembership> findAllByGroupIdAndDisabled(Long groupId, Boolean disabled);

    List<GroupMembership> findAllByMemberId(Long memberId);

    List<GroupMembership> findAllByMemberIdAndDisabled(Long memberId, Boolean disabled);

    List<GroupMembership> findAllByGroupRole(GroupRole groupRole);

    List<GroupMembership> findAllByGroupRoleAndDisabled(GroupRole groupRole, Boolean disabled);

    List<GroupMembership> findAllByGroupIdAndGroupRole(Long groupId, GroupRole groupRole);

    List<GroupMembership> findAllByGroupIdAndGroupRoleAndDisabled(Long groupId, GroupRole groupRole, Boolean disabled);

    List<GroupMembership> findAllByMemberIdAndGroupRole(Long memberId, GroupRole groupRole);

    List<GroupMembership> findAllByMemberIdAndGroupRoleAndDisabled(Long memberId,
                                                                   GroupRole groupRole,
                                                                   Boolean disabled);

    boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);

    boolean existsByGroupIdAndMemberIdAndDisabled(Long groupId, Long memberId, Boolean disabled);

    int countByGroupIdAndGroupRole(Long groupId, GroupRole groupRole);

    int countByGroupIdAndGroupRoleAndDisabled(Long groupId, GroupRole groupRole, Boolean disabled);

    int countByGroupIdAndGroupRoleIn(Long groupId, Set<GroupRole> groupRoles);

    int countByGroupIdAndGroupRoleInAndDisabled(Long groupId, Set<GroupRole> groupRoles, Boolean disabled);

    int countByGroupIdAndStatus(Long groupId, MembershipStatus status);

    int countByGroupIdAndStatusAndDisabled(Long groupId, MembershipStatus status, Boolean disabled);

    @Modifying
    @Query("UPDATE GroupMembership g SET g.disabled = :disabled WHERE g.groupId = :groupId")
    int updateDisabledForAllGroupMembership(@Param("groupId") Long groupId, @Param("disabled") Boolean disabled);

}
