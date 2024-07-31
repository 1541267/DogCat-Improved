package com.community.dogcat.jwt;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.community.dogcat.domain.User;
import com.community.dogcat.oauth2.CustomOAuth2Exception;
import com.community.dogcat.repository.user.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

	private static final int MAX_ATTEMPTS = 6;
	private final UserRepository userRepository;
	private final ConcurrentHashMap<String, Integer> loginAttempts = new ConcurrentHashMap<>();

	public CustomAuthenticationFailureHandler(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws
		IOException {

		if (exception instanceof CustomOAuth2Exception) {
			response.setContentType("text/html;charset=UTF-8");
			String errorMessage = exception.getMessage();
			response.getWriter().write(
				"<html><script>" + "alert('" + errorMessage + "');" + "window.location.href = '/user/login';"
					+ "</script></html>"
			);
			return;
		}

		String userId = request.getParameter("username");

		int attempts = loginAttempts.getOrDefault(userId, 0) + 1;
		loginAttempts.put(userId, attempts);

		if (attempts >= MAX_ATTEMPTS) {
			User userLockSet = userRepository.findById(userId).orElseThrow();
			userLockSet.setLoginLock(true);
			userRepository.save(userLockSet);
			loginAttempts.remove(userId); // 잠금 처리 후 실패 횟수 초기화

			response.setContentType("text/plain;charset=UTF-8");
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.getWriter().write("LOGIN_LOCKED");
			return;
		}

		response.setContentType("text/plain;charset=UTF-8");
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.getWriter().write("LOGIN_FAILED");
	}
}
