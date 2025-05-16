package com.community.dogcat.service.user;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.community.dogcat.domain.RefreshToken;
import com.community.dogcat.repository.user.RefreshRepository;

import lombok.RequiredArgsConstructor;

@Async
@Service
@RequiredArgsConstructor
// 비동리 리프레시 토큰 저장 서비스, LoginFilter에서 분리,
public class RefreshTokenService {

	private final RefreshRepository refreshRepository;

	/**
	 * @param username      사용자 아이디
	 * @param refreshToken  생성된 리프레시 토큰 문자열
	 * @param expiration    만료 시각 (ISO-8601 문자열 권장)
	 */

	public void saveToken(String username, String refreshToken, String expiration) {
		RefreshToken entitiy = RefreshToken.builder()
			.username(username)
			.refresh(refreshToken)
			.expiration(expiration).build();

		refreshRepository.save(entitiy);
	}
}
