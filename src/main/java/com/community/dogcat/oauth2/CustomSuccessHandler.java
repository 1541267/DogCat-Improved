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
import com.community.dogcat.domain.UsersAuth;
import com.community.dogcat.dto.user.CustomOAuth2User;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.repository.user.RefreshRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JWTUtil jwtUtil;
	private final RefreshRepository refreshRepository;
	private final UsersAuthRepository usersAuthRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		CustomOAuth2User customUserDetails = (CustomOAuth2User)authentication.getPrincipal();
		String userId = customUserDetails.getUserId();
		String role = getAuthoritiesByUserId(userId);

		String access = jwtUtil.createJwt("access", userId, role, 86400000L); //1day
		String refresh = jwtUtil.createJwt("refresh", userId, role, 604800000L); //1week

		addRefreshEntity(userId, refresh);

		response.addCookie(createCookie("access", access));
		response.addCookie(createCookie("refresh", refresh));
		response.sendRedirect("/");

	}

	private Cookie createCookie(String key, String value) {

		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(7 * 24 * 60 * 60);
		cookie.setHttpOnly(true);
		cookie.setPath("/");

		return cookie;
	}

	private void addRefreshEntity(String username, String refresh) {

		Date date = new Date(System.currentTimeMillis() + 604800000L);

		RefreshEntity refreshEntity = RefreshEntity.builder()
			.username(username).refresh(refresh).expiration(date.toString()).build();

		refreshRepository.save(refreshEntity);

	}

	public String getAuthoritiesByUserId(String userId) {

		UsersAuth usersAuth = usersAuthRepository.findByUserId(userId);

		if (usersAuth != null) {

			return usersAuth.getAuthorities();

		}

		return null;

	}

}