package com.app.backend.domain.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.app.backend.domain.member.controller.KakaoController;
import com.app.backend.domain.member.dto.kakao.KakaoUserInfo;
import com.app.backend.domain.member.dto.request.KakaoLoginRequestDto;
import com.app.backend.domain.member.dto.request.MemberJoinRequestDto;
import com.app.backend.domain.member.dto.request.MemberLoginRequestDto;
import com.app.backend.domain.member.dto.request.MemberModifyRequestDto;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.jwt.JwtProvider;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.member.service.KakaoAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@SpringBootTest           // 전체 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc     // MockMvc 자동 구성
@Transactional           // 테스트 후 롤백
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class MemberControllerTest {
	@Autowired
	private MockMvc mvc;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private ObjectMapper objectMapper;  // JSON 변환을 위한 객체
	@Autowired
	private JwtProvider jwtProvider;

	// 기존 회원 셋업
	private static final String setupUsername = "testID";
	private static final String setupPassword = "testPW";
	private static final String setupNickname = "김호남";
	private static final String setupRole = "ADMIN";

	private static final String newUsername = "username";
	private static final String newPassword = "password";
	private static final String newNickname = "김영남";
	private static final String newRole = "USER";

	@BeforeEach
	void 셋업() throws Exception {
		//given
		MemberJoinRequestDto request = new MemberJoinRequestDto(setupUsername, setupPassword, setupNickname, setupRole);

		//when
		mvc.perform(post("/api/v1/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())  // HTTP 상태코드 검증
			.andDo(print());  // 결과 출력
	}

	@Test
	@DisplayName("회원가입")
	void 회원가입() throws Exception {
		//given
		MemberJoinRequestDto request = new MemberJoinRequestDto(newUsername, newPassword, newNickname, newRole);

		//when
		mvc.perform(post("/api/v1/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())  // HTTP 상태코드 검증
			.andDo(print());  // 결과 출력

		//then
		// DB에 실제로 저장되었는지 확인
		Member savedMember = memberRepository.findByUsernameAndDisabled(newUsername, false)
			.orElseThrow(() -> new RuntimeException("회원이 저장되지 않았습니다."));

		assertAll(
			() -> assertEquals(request.username(), savedMember.getUsername()),
			() -> assertEquals(request.nickname(), savedMember.getNickname()),
			// 비밀번호는 암호화되어 저장되므로 직접 비교는 하지 않음
			() -> assertNotNull(savedMember.getPassword())
		);
	}

	@Test
	@DisplayName("관리자 로그인")
	void 관리자로그인() throws Exception {
		//given
		MemberLoginRequestDto request = new MemberLoginRequestDto(setupUsername, setupPassword);

		//when
		mvc.perform(post("/api/v1/members/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())  // HTTP 상태코드 검증
			.andDo(print());  // 결과 출력

		// then
		Member savedMember = memberRepository.findByUsernameAndDisabled(setupUsername, false)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다"));

		assertAll(
			() -> assertEquals(request.username(), savedMember.getUsername()),
			() -> assertNotNull(savedMember.getPassword())
		);
	}

	// @Test
	// @DisplayName("카카오 로그인")
	// void 카카오로그인() throws Exception {
	// 	// given
	// 	KakaoUserInfo kakaoUserInfo = new KakaoUserInfo("123", "kakao_123");
	// 	when(kakaoAuthService.getKakaoUserInfo(anyString()))
	// 		.thenReturn(kakaoUserInfo);
	//
	// 	//when
	// 	mvc.perform(post("/api/v1/members/kakao/callback")
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.content(objectMapper.writeValueAsString(
	// 				new KakaoLoginRequestDto("1", "김호남", "KAKAO"))))
	// 		.andExpect(status().isOk())
	// 		.andExpect(jsonPath("$.accessToken").exists())
	// 		.andExpect(jsonPath("$.refreshToken").exists())
	// 		.andDo(print());  // 결과 출력
	//
	// 	// then
	// 	Member savedMember = memberRepository.findByUsernameAndDisabled(setupUsername, false)
	// 		.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다"));
	//
	// 	assertAll(
	// 	);
	// }

	@Test
	@DisplayName("개인정보조회")
	void 개인정보조회() throws Exception{
		// given
		Member savedMember = memberRepository.findByUsernameAndDisabled(setupUsername, false)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다"));
		String token = jwtProvider.generateAccessToken(savedMember);

		// when
		mvc.perform(get("/api/v1/members/info")
			.header("Authorization", token)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("MEMBER_INFO_SUCCESS"))
			.andExpect(jsonPath("$.message").value("회원정보 조회에 성공했습니다"))
			.andDo(print());
	}

	@Test
	@DisplayName("개인정보 수정")
	void 개인정보수정() throws Exception {
		// given
		MemberModifyRequestDto request = new MemberModifyRequestDto(newNickname, newPassword);
		Member member = memberRepository.findByUsernameAndDisabled(setupUsername, false)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다"));
		String accessToken = "Bearer " + jwtProvider.generateAccessToken(member);

		// when
		mvc.perform(patch("/api/v1/members/modify")
				.header("Authorization", accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(print());

		// then
		Member savedMember = memberRepository.findByUsernameAndDisabled(setupUsername, false)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다"));

		assertAll(
			() -> assertNotEquals(request.nickname(), savedMember.getNickname()),
			() -> assertNotNull(savedMember.getPassword())
		);
	}

	@Test
	@DisplayName("모든 회원 조회 - 관리자만")
	void 모든회원조회() throws Exception {
		// given
		Member member = memberRepository.findByUsernameAndDisabled(setupUsername, false)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다"));

		if (!member.getRole().equals("ADMIN")) {
			fail("관리자 권한이 없습니다.");
		}
		String accessToken = "Bearer " + jwtProvider.generateAccessToken(member);

		// when
		mvc.perform(get("/api/v1/members/findAll")
				.header("Authorization", accessToken)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("회원 탈퇴")
	void 회원탈퇴() throws Exception {
		// given
		Member member = memberRepository.findByUsernameAndDisabled(setupUsername, false)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다"));
		String accessToken = "Bearer " + jwtProvider.generateAccessToken(member);

		// when
		mvc.perform(delete("/api/v1/members")
				.header("Authorization", accessToken)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("MEMBER_DELETE_SUCCESS"))
			.andExpect(jsonPath("$.message").value("회원 탈퇴에 성공했습니다"))
			.andDo(print());

		// then
		// disabled가 true로 변경되었는지 확인 (soft delete)
		assertThrows(IllegalArgumentException.class, () -> 
			memberRepository.findByUsernameAndDisabled(setupUsername, false)
				.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다"))
		);

		// disabled가 true인 회원이 존재하는지 확인
		Member deletedMember = memberRepository.findByUsernameAndDisabled(setupUsername, true)
			.orElseThrow(() -> new IllegalArgumentException("탈퇴한 회원을 찾을 수 없습니다"));

		assertAll(
			() -> assertTrue(deletedMember.isDisabled(), "회원이 비활성화되지 않았습니다"),
			() -> assertEquals(member.getUsername(), deletedMember.getUsername(), "username이 변경되었습니다"),
			() -> assertEquals(member.getNickname(), deletedMember.getNickname(), "nickname이 변경되었습니다"),
			() -> assertEquals(member.getRole(), deletedMember.getRole(), "role이 변경되었습니다")
		);
	}
}
