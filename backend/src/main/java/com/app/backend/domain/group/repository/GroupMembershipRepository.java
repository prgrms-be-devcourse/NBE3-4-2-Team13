package com.app.backend.domain.group.repository;

import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

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

}
