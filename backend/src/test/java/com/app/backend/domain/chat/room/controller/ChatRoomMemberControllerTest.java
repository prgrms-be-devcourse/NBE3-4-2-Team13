package com.app.backend.domain.chat.room.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.domain.chat.util.TestDataUtil;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.member.entity.Member;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ChatRoomMemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TestDataUtil testDataUtil;

	@Test
	@DisplayName("[성공] 채팅방 목록 조회")
	void getChatRoomsByMemberId() throws Exception {
		// given

		// 멤버 생성 & 저장
		Member savedMember = testDataUtil.createAndSaveMember("testUser", "testNickname");

		// 그룹 생성 & 저장
		Group group1 = testDataUtil.createAndSaveGroup(
			"대구fc 팬 모임1",
			"대구 광역시",
			"북구",
			"고성로 191",
			"대팍 직관 같이가실분"
		);
		Group group2 = testDataUtil.createAndSaveGroup(
			"대구fc 팬 모임2",
			"대구 광역시",
			"북구",
			"고성로 191",
			"대팍 직관 같이가실분2"
		);

		// 그룹과 멤버십 관계 설정
		GroupMembership groupMembership1 = testDataUtil.createAndSaveGroupMembership(savedMember, group1, GroupRole.LEADER);
		GroupMembership groupMembership2 = testDataUtil.createAndSaveGroupMembership(savedMember, group2, GroupRole.LEADER);

		// 채팅방 생성 및 그룹에 연결
		ChatRoom chatRoom1 = testDataUtil.createAndSaveChatRoom(group1);
		ChatRoom chatRoom2 = testDataUtil.createAndSaveChatRoom(group2);

		// 권한 처리
		testDataUtil.setAuthentication(savedMember);

		// when
		ResultActions resultActions = mockMvc.perform(get("/api/v1/members/chatrooms"));

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("채팅방 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data.length()").value(2)) // 두 개의 채팅방
			.andExpect(jsonPath("$.data[0].chatRoomId").value(1))
			.andExpect(jsonPath("$.data[0].groupId").value(1))
			.andExpect(jsonPath("$.data[0].groupName").value("대구fc 팬 모임1"))
			.andExpect(jsonPath("$.data[1].chatRoomId").value(2))
			.andExpect(jsonPath("$.data[1].groupId").value(2))
			.andExpect(jsonPath("$.data[1].groupName").value("대구fc 팬 모임2"));;
	}
}