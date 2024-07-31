package com.community.dogcat.jwt;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.community.dogcat.domain.User;
import com.community.dogcat.dto.user.CustomUserDetails;
import com.community.dogcat.service.user.ReissueService;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

	private final JWTUtil jwtUtil;
	private final ReissueService reissueService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String accessToken = extractJwtFromCookie(request);

		if (accessToken == null) {

			filterChain.doFilter(request, response);

			return;

		}

		try {

			jwtUtil.isExpired(accessToken);

		} catch (ExpiredJwtException e) {

			log.warn("Access token has expired");

			boolean reissued = reissueService.reissue(request, response);

			if (reissued) {

				response.sendRedirect("/");

			}

			return;

		}

		String category = jwtUtil.getCategory(accessToken);

		if (!category.equals("access")) {

			PrintWriter writer = response.getWriter();
			writer.print("Invalid access token");

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

			return;

		}

		User user = new User();

		CustomUserDetails customUserDetails = new CustomUserDetails(user, jwtUtil);
		Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authToken);

		filterChain.doFilter(request, response);

	}

	private String extractJwtFromCookie(HttpServletRequest request) {

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
