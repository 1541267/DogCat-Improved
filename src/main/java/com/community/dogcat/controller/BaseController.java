package com.community.dogcat.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.user.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseController {

	protected final JWTUtil jwtUtil;
	protected final UserService userService;

	@ModelAttribute
	public void addAttributes(HttpServletRequest request, Model model) {

		String accessToken = extractTokenFromCookie(request);

		if (accessToken != null) {

			String username = jwtUtil.getUsername(accessToken);
			String nickname = userService.getNickname(username);
			String role = userService.getRole(username);
			model.addAttribute("username", username);
			model.addAttribute("nickname", nickname);
			model.addAttribute("role", role);
			model.addAttribute("isLoggedIn", true);

		} else {
			
			model.addAttribute("username", null);
			model.addAttribute("nickname", null);
			model.addAttribute("role", null);
			model.addAttribute("isLoggedIn", false);

		}
	}

	protected String extractTokenFromCookie(HttpServletRequest request) {

		Cookie[] cookies = request.getCookies();

		if (cookies != null) {

			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("access")) {
					return cookie.getValue();
				}
			}

		}
		return null;

	}
}
