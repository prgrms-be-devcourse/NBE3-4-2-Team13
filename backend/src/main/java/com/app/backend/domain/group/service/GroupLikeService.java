package com.app.backend.domain.group.service;

import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupLike;
import com.app.backend.domain.group.exception.GroupLikeErrorCode;
import com.app.backend.domain.group.exception.GroupLikeException;
import com.app.backend.domain.group.repository.GroupLikeRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupLikeService {

    private final GroupLikeRepository groupLikeRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final EntityManager entityManager;

    /** 그룹 좋아요 여부 확인 */
    public boolean isLiked(final Long groupId, final Long memberId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupLikeException(GroupLikeErrorCode.GROUP_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GroupLikeException(GroupLikeErrorCode.MEMBER_NOT_FOUND));

        return groupLikeRepository.findByGroupAndMember(group, member).isPresent();
    }

    /** 그룹 좋아요 추가 */
    @Transactional
    public void likeGroup(final Long groupId, final Long memberId) {
        Group group = groupRepository.findByIdWithLock(groupId)
                .orElseThrow(() -> new GroupLikeException(GroupLikeErrorCode.GROUP_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GroupLikeException(GroupLikeErrorCode.MEMBER_NOT_FOUND));

        if (groupLikeRepository.findByGroupAndMember(group, member).isPresent()) {
            throw new GroupLikeException(GroupLikeErrorCode.ALREADY_LIKED);
        }

        GroupLike newLike = GroupLike.builder()
                .group(group)
                .member(member)
                .build();
        groupLikeRepository.save(newLike);
        entityManager.flush();
        group.increaseLikeCount();
    }

    /** 그룹 좋아요 취소 */
    @Transactional
    public void unlikeGroup(final Long groupId, final Long memberId) {
        Group group = groupRepository.findByIdWithLock(groupId)
                .orElseThrow(() -> new GroupLikeException(GroupLikeErrorCode.GROUP_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GroupLikeException(GroupLikeErrorCode.MEMBER_NOT_FOUND));

        GroupLike existingLike = groupLikeRepository.findByGroupAndMember(group, member)
                .orElseThrow(() -> new GroupLikeException(GroupLikeErrorCode.NOT_LIKED_YET));

        groupLikeRepository.delete(existingLike);
        group.decreaseLikeCount();
    }
}
