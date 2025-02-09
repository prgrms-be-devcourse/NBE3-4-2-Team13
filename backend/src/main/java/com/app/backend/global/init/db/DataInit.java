package com.app.backend.global.init.db;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.Member.Provider;
import com.app.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataInit {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupRepository groupRepository;
    private final CategoryRepository categoryRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void init() {

        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
        } catch (Exception e) {
            System.err.println("dev 모드 캐시 초기화 실패: " + e.getMessage());
        }

        Member dummyMember;
        if (memberRepository.findAll().stream().noneMatch(m -> m.getRole().equals("ROLE_ADMIN"))) {
            Member member = Member.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .nickname("admin")
                    .role("ROLE_ADMIN")
                    .provider(Provider.LOCAL)
                    .build();
            dummyMember = memberRepository.save(member);
        } else {
            dummyMember = memberRepository.findAll().get(0);
        }

        Category dummyCategory;
        if (categoryRepository.count() == 0) {
            Category category = Category.builder()
                    .name("축구")
                    .build();
            dummyCategory = categoryRepository.save(category);
        } else {
            return;
        }

        Group dummyGroup;
        if (groupRepository.count() == 0) {
            Group group = Group.builder()
                    .name("축구공은 둥글다")
                    .province("서울")
                    .city("서울시")
                    .town("강남구")
                    .description("열정적인 축구 동호회입니다.")
                    .recruitStatus(RecruitStatus.RECRUITING)
                    .maxRecruitCount(10)
                    .category(dummyCategory)
                    .build();
            dummyGroup = groupRepository.save(group);
        } else {
            return;
        }

        boolean membershipExists = groupMembershipRepository.findAll().stream()
                .anyMatch(gm -> gm.getMemberId().equals(dummyMember.getId())
                        && gm.getGroupId().equals(dummyGroup.getId()));
        if (!membershipExists) {
            GroupMembership membership = GroupMembership.builder()
                    .member(dummyMember)
                    .group(dummyGroup)
                    .groupRole(GroupRole.LEADER)
                    .build();
            groupMembershipRepository.save(membership);
        }
    }

}
