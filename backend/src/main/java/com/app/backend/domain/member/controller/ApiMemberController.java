package com.app.backend.domain.member.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.backend.domain.member.dto.request.MemberJoinRequestDto;
import com.app.backend.domain.member.dto.request.MemberLoginRequestDto;
import com.app.backend.domain.member.dto.request.MemberModifyRequestDto;
import com.app.backend.domain.member.dto.response.MemberJoinResponseDto;
import com.app.backend.domain.member.dto.response.MemberLoginResponseDto;
import com.app.backend.domain.member.dto.response.MemberModifyResponseDto;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.member.service.MemberService;
import com.app.backend.global.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class ApiMemberController {
	private final MemberService memberService;

	@Operation(summary = "관리자 회원가입", description = "관리자 회원 가입을 진행합니다")
	@PostMapping
	public ApiResponse<MemberJoinResponseDto> join(
		@RequestBody @Valid MemberJoinRequestDto request
	) {
		MemberJoinResponseDto response = memberService.createMember(request.username(), request.password(),
			request.nickname());
		return ApiResponse.of(
			true,
			"MEMBER_CREATE_SUCCESS",
			"회원가입이 성공적으로 완료되었습니다.",
			response
		);
	}

	@Operation(summary = "관리자 로그인", description = "관리자 로그인을 진행합니다")
	@PostMapping("/login")
	public ApiResponse<MemberLoginResponseDto> login(
		@RequestBody MemberLoginRequestDto request,
		HttpServletResponse response
	) {
		MemberLoginResponseDto loginResult = memberService.login(request);

		// 쿠키 설정
		Cookie accessTokenCookie = new Cookie("accessToken", loginResult.accessToken());
		Cookie refreshTokenCookie = new Cookie("refreshToken", loginResult.refreshToken());

		// 보안 설정
		accessTokenCookie.setHttpOnly(true);
		accessTokenCookie.setSecure(true);
		accessTokenCookie.setPath("/");
		accessTokenCookie.setDomain("localhost:3000");
		accessTokenCookie.setMaxAge(1800); // 30분

		String cookieHeader = String.format("%s; SameSite=Lax", accessTokenCookie.toString());
		response.setHeader("Set-Cookie", cookieHeader);
		response.addCookie(accessTokenCookie);
		response.addCookie(refreshTokenCookie);

		return ApiResponse.of(
			true,
			"MEMBER_LOGIN_SUCCESS",
			"로그인이 성공적으로 완료되었습니다.",
			loginResult
		);
	}

	@Operation(summary = "로그아웃", description = "클라이언트의 토큰 무효화 처리")
	@PostMapping("/logout")
	public ApiResponse<Void> logout (
		@RequestHeader(value = "Authorization") String token
	) {
		memberService.logout(token);
		return ApiResponse.of(
			true,
			"MEMBER_LOGOUT_SUCCESS",
			"로그아웃이 성공적으로 완료되었습니다."
		);
	}

	@Operation(summary = "개인정보 조회", description = "JWT 토큰을 이용해 회원 정보를 조회합니다.")
	// @Parameter(name = "Authorization", description = "Bearer JWT token", required = true, in = ParameterIn.HEADER)
	@GetMapping("/info")
	public ApiResponse<MemberDetails> getMemberInfo(
		@RequestHeader(value = "Authorization") String token
	) {
		Member member = memberService.getCurrentMember(token); // 현재 사용자 조회
		return ApiResponse.of(
			true, "MEMBER_INFO_SUCCESS", "회원정보 조회에 성공했습니다", MemberDetails.of(member)
		);
	}

	@Operation(summary = "개인정보 수정", description = "관리자 회원 정보를 수정합니다")
	@PatchMapping("/modify")
	public ApiResponse<MemberModifyResponseDto> modifyMemberInfo(
		@RequestHeader(value = "Authorization") String token,
		@RequestBody MemberModifyRequestDto request
	) {
		Member member = memberService.getCurrentMember(token); // 현재 사용자 조회
		MemberModifyResponseDto response = memberService.modifyMember(member, request);
		return ApiResponse.of(true, "MEMBER_MODIFY_SUCCESS", "회원정보 수정에 성공했습니다", response);
	}

	@Operation(summary = "모든 회원 조회", description = "관리자가 모든 회원을 조회합니다")
	@GetMapping("/findAll")
	public ApiResponse<Optional<List<Member>>> findAll(
		@RequestHeader("Authorization") String token
	) {
		Optional<List<Member>> allMembers = memberService.findAllMembers(token);
		return ApiResponse.of(
			true,
			"MEMBER_FIND_ALL_SUCCESS",
			"모든 회원을 조회했습니다",
			allMembers);
	}

	@Operation(summary = "회원 탈퇴 (soft delete)", description = "가입자가 회원 탈퇴를 하면 disabled 상태가 됩니다")
	@DeleteMapping
	public ApiResponse<Void> deleteMember(
		@RequestHeader("Authorization") String token
	) {
		memberService.deleteMember(token);
		return ApiResponse.of(
			true,
			"MEMBER_DELETE_SUCCESS",
			"회원 탈퇴에 성공했습니다"
		);
	}
}
