package com.app.backend.domain.meetingApplication.meetingApplicationControllerTest;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationDto;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationListDto;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationReqBody;
import com.app.backend.domain.meetingApplication.entity.MeetingApplication;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationErrorCode;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationException;
import com.app.backend.domain.meetingApplication.repository.MeetingApplicationRepository;
import com.app.backend.domain.meetingApplication.service.MeetingApplicationService;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MeetingApplicationControllerTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private GroupMembershipRepository groupMembershipRepository;

	@Autowired
	private MeetingApplicationRepository meetingApplicationRepository;

	@MockitoBean
	private MeetingApplicationService meetingApplicationService;

	@Autowired
	MockMvc mvc;

	private Group group;
	private Member member;

	@BeforeEach
	void setup() {
		group = groupRepository.save(Group.builder()
			.name("test group")
			.province("test province")
			.city("test city")
			.town("test town")
			.description("test description")
			.recruitStatus(RecruitStatus.RECRUITING)
			.maxRecruitCount(1)
			.build());

		member = Member.builder()
			.username("testUser")
			.nickname("testNickname")
			.role("USER")
			.disabled(false)
			.build();

		groupRepository.save(group);
		memberRepository.save(member);
	}

	@Test
	@DisplayName("신청 폼 작성")
	void t1() throws Exception {
		MemberDetails mockUser = new MemberDetails(member);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("신청합니다.");

		MeetingApplication mockMeetingApplication = MeetingApplication.builder()
			.id(1L)
			.context("신청합니다.")
			.group(group)
			.member(member)
			.build();

		given(meetingApplicationService.create(group.getId(), request, member.getId()))
			.willReturn(mockMeetingApplication);

		// When
		ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/api/v1/groups/{groupId}", group.getId())
			.with(user(mockUser))  // MockUser를 사용하여 로그인된 상태 시뮬레이션
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request)) // 요청 본문
		);

		// Then
		resultActions.andExpect(status().isCreated());
		resultActions.andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()));
		resultActions.andExpect(jsonPath("$.message")
			.value(group.getId() + "번 모임에 성공적으로 가입 신청을 하셨습니다."));
		resultActions.andExpect(jsonPath("$.data.context").value("신청합니다."));
		resultActions.andDo(print());

		verify(meetingApplicationService, times(1)).create(group.getId(), request, member.getId());

	}

	@Test
	@DisplayName("신청 폼 제출 - 그룹 정원 초과 시 예외 처리")
	void t2() throws Exception {
		// Given
		groupMembershipRepository.save(GroupMembership.builder()
			.group(group)
			.member(member)
			.groupRole(GroupRole.LEADER)
			.build());

		Member newMember = Member.builder()
			.username("newUser")
			.nickname("newNickname")
			.role("USER")
			.disabled(false)
			.build();
		memberRepository.save(newMember);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("Test Application");

		given(meetingApplicationService.create(group.getId(), request, newMember.getId()))
			.willThrow(new MeetingApplicationException(MeetingApplicationErrorCode.GROUP_MEMBER_LIMIT_EXCEEDED));

		MemberDetails mockUser = new MemberDetails(newMember);

		// When & Then
		mvc.perform(MockMvcRequestBuilders.post("/api/v1/groups/{groupId}", group.getId())
				.with(user(mockUser))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.header("Authorization", "Bearer testToken"))
			.andExpect(status().isConflict())
			.andExpect(result -> {
				String responseContent = result.getResponse().getContentAsString();
				assert(responseContent.contains("그룹 정원이 초과되었습니다."));
			});
	}

	@Test
	@DisplayName("meeting application 조회")
	void t3() throws Exception {
		// Given
		Member leader = memberRepository.save(Member.builder()
			.username("testUser")
			.nickname("testNickname")
			.role("USER")
			.disabled(false)
			.build());

		MemberDetails mockUser = new MemberDetails(leader);

		GroupMembership groupMembership = groupMembershipRepository.save(GroupMembership.builder()
			.group(group)
			.member(leader)
			.groupRole(GroupRole.LEADER)
			.build());

		meetingApplicationRepository.save(MeetingApplication.builder()
			.group(group)
			.member(member)
			.context("Test Application")
			.build());

		// When & Then
		mvc.perform(get("/api/v1/groups/{groupId}/meeting_application", group.getId())
				.with(user(mockUser))  // 인증된 사용자 설정
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("meeting application 조회 성공"));
	}

	@Test
	@DisplayName("meeting application 상세 조회")
	void t4() throws Exception {
		// Given
		Member leader = memberRepository.save(Member.builder()
			.username("testUser")
			.nickname("testNickname")
			.role("USER")
			.disabled(false)
			.build());

		MemberDetails mockUser = new MemberDetails(leader);

		group = groupRepository.save(Group.builder()
			.name("test group")
			.province("test province")
			.city("test city")
			.town("test town")
			.description("test description")
			.recruitStatus(RecruitStatus.RECRUITING)
			.maxRecruitCount(10)
			.build());

		GroupMembership groupMembership = groupMembershipRepository.save(GroupMembership.builder()
			.group(group)
			.member(leader)
			.groupRole(GroupRole.LEADER)
			.build());

		meetingApplicationRepository.save(MeetingApplication.builder()
			.group(group)
			.member(member)
			.context("Test Application")
			.build());

		// When & Then
		mvc.perform(get("/api/v1/groups/{groupId}/meeting_application/{meetingApplicationId}", group.getId(), 1)
				.with(user(mockUser))  // 인증된 사용자 설정
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("meeting application 상세 조회 성공"));

	}

}
