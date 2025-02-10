package com.app.backend.global.init.db;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.domain.chat.room.repository.ChatRoomRepository;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.Member.Provider;
import com.app.backend.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.Random;
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

    private final MemberRepository              memberRepository;
    private final PasswordEncoder               passwordEncoder;
    private final GroupRepository               groupRepository;
    private final CategoryRepository            categoryRepository;
    private final ChatRoomRepository            chatRoomRepository;
    private final GroupMembershipRepository     groupMembershipRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void init() {
        groupRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();

        Random random = new Random();

        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
        } catch (Exception e) {
            System.err.println("dev 모드 캐시 초기화 실패: " + e.getMessage());
        }

        if (memberRepository.findAll().stream().noneMatch(m -> m.getRole().equals("ROLE_ADMIN"))) {
            Member member = Member.builder()
                                  .username("admin")
                                  .password(passwordEncoder.encode("admin"))
                                  .nickname("admin")
                                  .role("ROLE_ADMIN")
                                  .provider(Provider.LOCAL)
                                  .build();
            memberRepository.save(member);
        }

        List<String> categories = List.of("운동", "축구", "농구", "야구", "음식", "게임", "여행");
        for (String name : categories) {
            Category category = Category.builder()
                                        .name(name)
                                        .build();
            categoryRepository.save(category);
        }

        int i = 1;
        List<String> addresses = List.of("서울 서초구 서초동", "서울 서초구 잠원동", "서울 서초구 반포동", "서울 서초구 방배동",
                                         "경기 성남시 분당구", "경기 수원시 권선구", "경기 안양시 만안구", "인천 남동구 구월동",
                                         "대전 중구 은행동", "대구 수성구 두산동", "광주 동구 용연동", "부산 수영구 광안동",
                                         "강원특별자치도 강릉시 송정동", "제주특별자치도 서귀포시 성산읍");
        while (i <= 100) {
            String   name     = categories.get(random.nextInt(categories.size()));
            Category category = categoryRepository.findByNameAndDisabled(name, false).get();

            Member member = Member.builder()
                                  .username("user".formatted(i))
                                  .password(passwordEncoder.encode("user%d".formatted(i)))
                                  .nickname("user%d".formatted(i))
                                  .role("ROLE_USER")
                                  .provider(Provider.LOCAL)
                                  .build();
            memberRepository.save(member);

            String[] splitAddress = addresses.get(random.nextInt(addresses.size())).split(" ");
            Group group = Group.builder()
                               .name("랜덤모임%d".formatted(i))
                               .province(splitAddress[0])
                               .city(splitAddress[1])
                               .town(splitAddress[2])
                               .description("%s 랜덤모임%d 입니다.".formatted(name, i))
                               .maxRecruitCount(random.nextInt(49) + 1)
                               .recruitStatus(RecruitStatus.RECRUITING)
                               .category(category)
                               .build();
            groupRepository.save(group);

            GroupMembership groupMembership = GroupMembership.builder()
                                                             .member(member)
                                                             .group(group)
                                                             .groupRole(GroupRole.LEADER)
                                                             .build();
            groupMembershipRepository.save(groupMembership);

            ChatRoom chatRoom = ChatRoom.builder()
                                        .group(group)
                                        .build();
            group.setChatRoom(chatRoom);

            chatRoomRepository.save(chatRoom);
            i++;
        }
    }

}
