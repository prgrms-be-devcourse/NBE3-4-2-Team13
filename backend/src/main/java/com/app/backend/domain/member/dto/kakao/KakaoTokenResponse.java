package com.app.backend.domain.member.dto.kakao;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoTokenResponse {
	private String access_token;
	private String token_type;
	private String refresh_token;
	private String expires_in;
	private String scope;
}
