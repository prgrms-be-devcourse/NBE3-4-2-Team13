package com.app.backend.domain.group.supporter;

import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.group.service.GroupService;
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
    protected GroupService groupService;

}
