package com.community.dogcat.jwt;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.community.dogcat.domain.RefreshToken;
import com.community.dogcat.domain.User;
import com.community.dogcat.domain.UsersAuth;
import com.community.dogcat.repository.user.RefreshRepository;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;
import com.community.dogcat.service.user.RefreshTokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginFilter수정된백업 extends UsernamePasswordAuthenticationFilter {

	private final JWTUtil jwtUtil;
	private final UserRepository userRepository;
	private final RefreshRepository refreshRepository;
	private final UsersAuthRepository usersAuthRepository;
	private final AuthenticationManager authenticationManager;
	private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

	// 개선
	private final RefreshTokenService refreshTokenService;

	public LoginFilter수정된백업(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
		RefreshRepository refreshRepository, UsersAuthRepository usersAuthRepository, UserRepository userRepository,
		CustomAuthenticationFailureHandler customAuthenticationFailureHandler, RefreshTokenService refreshTokenService) {

		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.refreshRepository = refreshRepository;
		this.usersAuthRepository = usersAuthRepository;
		this.userRepository = userRepository;
		this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
		this.refreshTokenService = refreshTokenService;
		setFilterProcessesUrl("/user/loginProc");

	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {

		String username = obtainUsername(request);
		String password = obtainPassword(request);

		boolean autoLogin = "true".equals(request.getParameter("autoLogin"));
		boolean rememberMe = "true".equals(request.getParameter("rememberMe"));

		Map<String, Boolean> loginDetails = new HashMap<>();
		loginDetails.put("autoLogin", autoLogin);
		loginDetails.put("rememberMe", rememberMe);

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password,
			null);
		authToken.setDetails(loginDetails);

		return authenticationManager.authenticate(authToken);
	}

	@Override
	// 개선, 기존엔 사용자 조회, 경험치 증가 & 저장, jwt 토큰 생성, 리프레시 토큰(db)저장, 권한 조회
	// 입 출력만 4번(조회,업데이트,인서트,조회) 발생
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authentication) throws IOException {

		@SuppressWarnings("unchecked")
		Map<String, Boolean> loginDetails = (Map<String, Boolean>)authentication.getDetails();

		Boolean autoLogin = loginDetails.get("autoLogin");
		Boolean rememberMe = loginDetails.get("rememberMe");
		String userId = authentication.getName();
		String role = getAuthoritiesByUserId(userId);

		User user = userRepository.findByUserId(userId);

		if (user == null) {
			response.sendError(HttpStatus.UNAUTHORIZED.value(), "INVALID_USER");
			return;
		}

		if (user.isLoginLock()) {
			response.setContentType("text/plain;charset=UTF-8");
			response.setStatus(HttpStatus.FORBIDDEN.value()); // 403 Forbidden 상태 코드 반환
			response.getWriter().write("LOGIN_LOCKED");
			return;
		}

		if (user.isBlock()) {
			response.setContentType("text/plain;charset=UTF-8");
			response.setStatus(HttpStatus.FORBIDDEN.value()); // 403 Forbidden 상태 코드 반환
			response.getWriter().write("LOGIN_BLOCK");
			return;
		}

		// 개선, 2) 경험치 증가 합쳐서 단일 update jpql 호출
		// user.incrementExp();
		// userRepository.save(user);
		// 리프래시 토큰 저장 비동기 실행
		userRepository.incrementExpByUserId(userId);

		String access = jwtUtil.createJwt("access", userId, role, 86400000L); //1day
		String refresh = jwtUtil.createJwt("refresh", userId, role, 604800000L); //1week
		String expiration = DateTimeFormatter.ISO_LOCAL_DATE_TIME
			.withZone(ZoneId.systemDefault())
			.format(Instant.now().plusMillis(604_800_000L));
		// addRefreshToken(userId, refresh);
		// 4) 리프레시 토큰 저장 비동기 실행
		refreshTokenService.saveToken(user.getUserName(), refresh, expiration);

		if (autoLogin) {
			response.addCookie(createCookie("access", access));
			response.addCookie(createCookie("refresh", refresh));
			response.setStatus(HttpStatus.OK.value());
		} else {
			response.addCookie(createCookie("access", access));
			response.setStatus(HttpStatus.OK.value());
		}

		if (rememberMe) {
			Cookie cookie = new Cookie("userId", userId);
			cookie.setMaxAge(7 * 24 * 60 * 60);
			cookie.setPath("/");
			response.addCookie(cookie);
		} else {
			Cookie cookie = new Cookie("userId", null);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
		}
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException {

		customAuthenticationFailureHandler.onAuthenticationFailure(request, response, failed);

	}

	private void addRefreshToken(String username, String refresh) {

		Date date = new Date(System.currentTimeMillis() + 604800000L);

		RefreshToken refreshToken = RefreshToken.builder()
			.username(username)
			.refresh(refresh)
			.expiration(date.toString())
			.build();

		refreshRepository.save(refreshToken);

	}

	private Cookie createCookie(String key, String value) {

		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(7 * 24 * 60 * 60); //1week
		cookie.setHttpOnly(true);
		cookie.setPath("/");

		return cookie;

	}

	public String getAuthoritiesByUserId(String userId) {

		UsersAuth usersAuth = usersAuthRepository.findByUserId(userId);

		if (usersAuth != null) {

			return usersAuth.getAuthorities();

		}

		return null;

	}

}
