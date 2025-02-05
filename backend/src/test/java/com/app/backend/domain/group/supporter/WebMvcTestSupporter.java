package com.app.backend.domain.group.supporter;

import com.app.backend.domain.group.service.GroupMembershipService;
import com.app.backend.domain.group.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
//@Import(SecurityConfig.class)
//@WebMvcTest(GroupController.class)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class WebMvcTestSupporter {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected GroupService groupService;

    @MockitoBean
    protected GroupMembershipService groupMembershipService;

}
