package com.app.backend.domain.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.app.backend.domain.member.controller.KakaoController;
import com.app.backend.domain.member.dto.request.MemberJoinRequestDto;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.member.service.KakaoAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@SpringBootTest           // 전체 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc     // MockMvc 자동 구성
@Transactional           // 테스트 후 롤백
@ActiveProfiles("test")
public class MemberControllerTest {
	@Autowired
	private MockMvc mvc;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private ObjectMapper objectMapper;  // JSON 변환을 위한 객체
	@Autowired
	private KakaoAuthService kakaoAuthService;
	@Autowired
	private KakaoController kakaoController;

	// @BeforeEach
	// void setUp() {
	// 	kakaoAuthService = spy(kakaoAuthService);
	// 	kakaoController = spy(kakaoController);
	// }
	//
	// @Test
	// @DisplayName("회원가입") // 아직 안됨
	// void 회원가입() throws Exception {
	// 	//given
	// 	MemberJoinRequestDto request = new MemberJoinRequestDto("testID", "testPW", "김호남");
	//
	// 	//when
	// 	mvc.perform(post("/api/members/join")
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.content(objectMapper.writeValueAsString(request)))
	// 		.andExpect(status().isOk())  // HTTP 상태코드 검증
	// 		.andDo(print());  // 결과 출력
	//
	// 	//then
	// 	// DB에 실제로 저장되었는지 확인
	// 	Member savedMember = memberRepository.findByUsername("testID")
	// 		.orElseThrow(() -> new RuntimeException("회원이 저장되지 않았습니다."));
	//
	// 	assertAll(
	// 		() -> assertEquals(request.username(), savedMember.getUsername()),
	// 		() -> assertEquals(request.nickname(), savedMember.getNickname()),
	// 		// 비밀번호는 암호화되어 저장되므로 직접 비교는 하지 않음
	// 		() -> assertNotNull(savedMember.getPassword())
	// 	);
	// }
}
