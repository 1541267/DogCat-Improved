package com.community.dogcat.dto.user;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.Cookie;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.community.dogcat.domain.User;
import com.community.dogcat.jwt.JWTUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

	private final User user;
	private final JWTUtil jwtUtil;

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		String token = getTokenFromCookie();
		if (token == null || token.isEmpty()) {
			// 예외 처리: 토큰이 없을 경우 기본 역할 반환 또는 예외 던지기
			return List.of(); // 기본값으로 빈 권한 반환
		}
		String role = jwtUtil.getRole(token); // JWTUtil 로 ROLE 가져오기
		return List.of(() -> role); // 단일 권한으로 반환
	}

	private String getTokenFromCookie() {
		ServletRequestAttributes attrs = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		Cookie[] cookies = attrs.getRequest().getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("access")) { // 쿠키 이름을 확인
					return cookie.getValue(); // JWT 토큰 반환
				}
			}
		}
		return null; // 쿠키가 없을 경우 null 반환
	}

	@Override
	public String getPassword() {
		return user.getUserPw();
	}

	@Override
	public String getUsername() {
		return user.getUserId();
	}

}
