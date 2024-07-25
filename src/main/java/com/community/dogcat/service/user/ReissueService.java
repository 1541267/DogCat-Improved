package com.community.dogcat.service.user;

import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.community.dogcat.domain.RefreshEntity;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.repository.user.RefreshRepository;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReissueService {

	private final JWTUtil jwtUtil;
	private final RefreshRepository refreshRepository;

	public boolean reissue(HttpServletRequest request, HttpServletResponse response) {

		Cookie[] cookies = request.getCookies();

		String refresh = null;

		if (cookies != null) {

			for (Cookie cookie : cookies) {

				if (cookie.getName().equals("refresh")) {

					refresh = cookie.getValue();

				}
			}

		}

		if (refresh == null) {

			Cookie accessCookie = new Cookie("access", null);

			accessCookie.setMaxAge(0);
			accessCookie.setPath("/");
			response.addCookie(accessCookie);

			return false;

		}

		try {

			jwtUtil.isExpired(refresh);

		} catch (ExpiredJwtException e) {

			log.info("Refresh token has expired");

			return false;

		}

		String category = jwtUtil.getCategory(refresh);

		if (!category.equals("refresh")) {

			log.info("Invalid refresh token");

			return false;

		}

		boolean isExist = refreshRepository.existsByRefresh(refresh);

		if (!isExist) {

			log.info("Invalid refresh token");

			return false;

		}

		String username = jwtUtil.getUsername(refresh);
		String role = jwtUtil.getRole(refresh);

		String newAccess = jwtUtil.createJwt("access", username, role, 86400000L); // 1 day
		String newRefresh = jwtUtil.createJwt("refresh", username, role, 604800000L); // 1 week

		refreshRepository.deleteByRefresh(refresh);
		addRefreshEntity(username, newRefresh);

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("refresh") || cookie.getName().equals("access")) {
				cookie.setMaxAge(0);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		}

		response.addCookie(createCookie("access", newAccess));
		response.addCookie(createCookie("refresh", newRefresh));

		return true;

	}

	private void addRefreshEntity(String username, String refresh) {

		Date date = new Date(System.currentTimeMillis() + 604800000L);

		RefreshEntity refreshEntity = RefreshEntity.builder()
			.username(username)
			.refresh(refresh)
			.expiration(date.toString())
			.build();

		refreshRepository.save(refreshEntity);

	}

	private Cookie createCookie(String key, String value) {

		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(7 * 24 * 60 * 60);
		cookie.setHttpOnly(true);
		cookie.setPath("/");

		return cookie;

	}

}
