package com.community.dogcat.oauth2;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.community.dogcat.domain.RefreshEntity;
import com.community.dogcat.dto.user.CustomOAuth2User;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.repository.user.RefreshRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JWTUtil jwtUtil;
	private final RefreshRepository refreshRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		//OAuth2User
		CustomOAuth2User customUserDetails = (CustomOAuth2User)authentication.getPrincipal();

		String userId = customUserDetails.getUserId();

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
		GrantedAuthority auth = iterator.next();
		String role = auth.getAuthority();

		// 두 개의 토큰 발급
		// 1일 (86,400,000 milliseconds)
		String access = jwtUtil.createJwt("access", userId, role, 86400000L);

		// 1주일 (604,800,000 milliseconds)
		String refresh = jwtUtil.createJwt("refresh", userId, role, 604800000L);

		//refresh 토큰 데이터베이스에 저장! 또 쓰면 안되니까 검증하려고
		addRefreshEntity(userId, refresh);

		response.addCookie(createCookie("access", access));
		response.addCookie(createCookie("refresh", refresh));
		response.sendRedirect("/sample/home");

	}

	private Cookie createCookie(String key, String value) {
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(7 * 24 * 60 * 60); // 1주일 유효 기간 설정 (초 단위)
		cookie.setHttpOnly(true); // JavaScript 에서 접근 불가 설정
		cookie.setPath("/"); // 모든 경로에서 접근 가능하도록 설정

		return cookie;
	}

	private void addRefreshEntity(String username, String refresh) {
		//refreshEntity 를 데이터베이스에 저장하기 위해 만든 메서드

		Date date = new Date(System.currentTimeMillis() + 604800000L);

		RefreshEntity refreshEntity = RefreshEntity.builder()
			.username(username).refresh(refresh).expiration(date.toString()).build();

		refreshRepository.save(refreshEntity);
	}
}