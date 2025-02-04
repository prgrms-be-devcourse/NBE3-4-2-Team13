package com.app.backend.domain.group.supporter;

import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.chat.room.repository.ChatRoomRepository;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.group.service.GroupService;
import com.app.backend.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public abstract class SpringBootTestSupporter {

    @Autowired
    protected EntityManager em;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected GroupMembershipRepository groupMembershipRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected ChatRoomRepository chatRoomRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected GroupService groupService;

}
