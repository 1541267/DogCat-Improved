package com.community.dogcat.dto.social;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KakaoResponse implements OAuth2Response {

	private final Map<String, Object> attribute;

	@Override
	public String getProvider() {
		return "kakao";
	}

	@Override
	public String getProviderId() {
		return attribute.get("id").toString();
	}

	@Override
	public String getEmail() {
		Map<String, Object> kakaoAccount = (Map<String, Object>)attribute.get("kakao_account");
		return kakaoAccount.get("email").toString();
	}

	@Override
	public String getName() {
		Map<String, Object> properties = (Map<String, Object>)attribute.get("properties");
		return properties.get("nickname").toString();
	}
}
