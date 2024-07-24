package com.community.dogcat.jwt;

import java.io.IOException;
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

import com.community.dogcat.domain.RefreshEntity;
import com.community.dogcat.domain.User;
import com.community.dogcat.domain.UsersAuth;
import com.community.dogcat.repository.user.RefreshRepository;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
	//JWT 방식 사용을 위해 SecurityConfig 에서 formLogin 을 disable 시켰기 때문에 UsernamePasswordAuthenticationFilter 을 따로 만들어줘야함

	private final AuthenticationManager authenticationManager;
	private final JWTUtil jwtUtil;
	private final RefreshRepository refreshRepository;
	private final UsersAuthRepository usersAuthRepository;
	private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
	private final UserRepository userRepository;

	public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
		RefreshRepository refreshRepository, UsersAuthRepository usersAuthRepository, UserRepository userRepository,
		CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.refreshRepository = refreshRepository;
		this.usersAuthRepository = usersAuthRepository;
		this.userRepository = userRepository;
		this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
		setFilterProcessesUrl("/user/loginProc");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		//여기서 authenticationManager 에게 로그인 정보가 담겨있는 인증토큰을 넘겨줌

		//클라이언트 요청에서 userId, userPw 추출
		String username = obtainUsername(request);
		String password = obtainPassword(request);

		// 클라이언트에서 전송된 자동로그인 및 아이디 저장 체크 여부 확인
		boolean autoLogin = "true".equals(request.getParameter("autoLogin"));
		boolean rememberMe = "true".equals(request.getParameter("rememberMe"));

		// details 필드에 값을 저장할 Map 객체 생성
		Map<String, Boolean> loginDetails = new HashMap<>();
		loginDetails.put("autoLogin", autoLogin);
		loginDetails.put("rememberMe", rememberMe);

		//스프링 시큐리티에서 userId, userPw 를 검증하기 위해서는 토큰에 담아야 함
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password,
			null);
		authToken.setDetails(loginDetails); // 자동로그인 여부를 토큰의 details 에 저장

		//토큰을 던져주면 authenticationManager 가 검증을 담당
		return authenticationManager.authenticate(authToken);
	}

	//로그인 성공 시 실행하는 메서드 (여기서 JWT 발급을 진행하면 됨)
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authentication) {

		// details 필드에서 Map 객체 가져오기
		@SuppressWarnings("unchecked")
		Map<String, Boolean> loginDetails = (Map<String, Boolean>)authentication.getDetails();

		// autoLogin 및 rememberMe 값 추출
		Boolean autoLogin = loginDetails.get("autoLogin");
		Boolean rememberMe = loginDetails.get("rememberMe");

		//먼저 authentication 에서 유저정보를 가져옴 username, role
		String userId = authentication.getName();
		//권한정보를 가져옴
		String role = getAuthoritiesByUserId(userId);

		User user = userRepository.findByUserId(userId);

		//user 테이블의 loginLock 값이 true 이면 로그인 막기
		if (user != null && user.isLoginLock()) {
			try {
				response.setContentType("text/plain;charset=UTF-8");
				response.setStatus(HttpStatus.FORBIDDEN.value()); // 403 Forbidden 상태 코드 반환
				response.getWriter().write("LOGIN_LOCKED"); // 클라이언트에서 이 문자열을 받아서 처리할 예정
			} catch (IOException e) {
				log.error("IOException occurred while redirecting", e);
			}
			return; // 메서드를 빠져나감
		}

		//user 테이블의 block 값이 true 이면 로그인 막기
		if (user != null && user.isBlock()) {
			try {
				response.setContentType("text/plain;charset=UTF-8");
				response.setStatus(HttpStatus.FORBIDDEN.value()); // 403 Forbidden 상태 코드 반환
				response.getWriter().write("LOGIN_BLOCK"); // 클라이언트에서 이 문자열을 받아서 처리할 예정
			} catch (IOException e) {
				log.error("IOException occurred while redirecting", e);
			}
			return; // 메서드를 빠져나감
		}

		// exp 값 증가
		if (user != null) {
			user.incrementExp();
			userRepository.save(user);
		}

		// 하루 (86,400,000 milliseconds)
		String access = jwtUtil.createJwt("access", userId, role, 86400000L);
		// 일주일 (604,800,000 milliseconds)
		String refresh = jwtUtil.createJwt("refresh", userId, role, 604800000L);

		//refresh 토큰 데이터베이스에 저장! 또 쓰면 안되니까 검증하려고
		addRefreshEntity(userId, refresh);

		//자동로그인 설정
		if (autoLogin) {
			response.addCookie(createCookie("access", access));
			response.addCookie(createCookie("refresh", refresh));
			response.setStatus(HttpStatus.OK.value());
		} else {
			response.addCookie(createCookie("access", access));
			response.setStatus(HttpStatus.OK.value());
		}

		// rememberMe 설정
		if (rememberMe) {
			Cookie cookie = new Cookie("userId", userId);
			cookie.setMaxAge(7 * 24 * 60 * 60); // 1주일 유효기간 설정
			cookie.setPath("/");
			response.addCookie(cookie);
		} else {
			Cookie cookie = new Cookie("userId", null);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
		}
	}

	//로그인 실패 시 실행하는 메서드
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException {

		customAuthenticationFailureHandler.onAuthenticationFailure(request, response, failed);
	}

	private void addRefreshEntity(String username, String refresh) {
		// 현재 시간에 일주일을 더한 시간 계산
		Date date = new Date(System.currentTimeMillis() + 604800000L);

		RefreshEntity refreshEntity = RefreshEntity.builder()
			.username(username)
			.refresh(refresh)
			.expiration(date.toString()) // 날짜를 문자열로 저장
			.build();

		refreshRepository.save(refreshEntity);
	}

	private Cookie createCookie(String key, String value) {
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(7 * 24 * 60 * 60); // 1주일 유효 기간 설정 (초 단위)
		cookie.setHttpOnly(true); // JavaScript 에서 접근 불가 설정
		cookie.setPath("/"); // 모든 경로에서 접근 가능하도록 설정

		return cookie;
	}

	public String getAuthoritiesByUserId(String userId) {
		UsersAuth usersAuth = usersAuthRepository.findByUserId(userId);
		if (usersAuth != null) {
			return usersAuth.getAuthorities();
		}
		return null; // 권한이 없으면 null 반환
	}

}
