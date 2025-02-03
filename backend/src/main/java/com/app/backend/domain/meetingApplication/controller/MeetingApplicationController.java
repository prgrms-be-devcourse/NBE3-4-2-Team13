package com.app.backend.domain.meetingApplication.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.backend.domain.meetingApplication.dto.MeetingApplicationDto;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationReqBody;
import com.app.backend.domain.meetingApplication.entity.MeetingApplication;
import com.app.backend.domain.meetingApplication.service.MeetingApplicationService;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.global.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class MeetingApplicationController {

	private final MeetingApplicationService meetingApplicationService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/{groupId}")
	public ApiResponse<MeetingApplicationDto> createMeetingApplication(
		@PathVariable Long groupId,
		@RequestBody MeetingApplicationReqBody request,
		@AuthenticationPrincipal MemberDetails memberDetails
	) {
		MeetingApplication meetingApplication = meetingApplicationService.create(groupId, request, memberDetails.getId());

		MeetingApplicationDto meetingApplicationDto = MeetingApplicationDto.from(meetingApplication);

		return ApiResponse.of(
			true,
			"201",
			"%d번 모임에 성공적으로 가입 신청을 하셨습니다.".formatted(meetingApplication.getGroup().getId()),
			meetingApplicationDto
		);
	}

}
