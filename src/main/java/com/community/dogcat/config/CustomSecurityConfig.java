package com.community.dogcat.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.context.request.RequestContextListener;

import com.community.dogcat.jwt.CustomAuthenticationFailureHandler;
import com.community.dogcat.jwt.CustomLogoutFilter;
import com.community.dogcat.jwt.JWTFilter;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.jwt.LoginFilter;
import com.community.dogcat.oauth2.CustomSuccessHandler;
import com.community.dogcat.repository.user.RefreshRepository;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;
import com.community.dogcat.service.user.CustomOAuth2UserService;
import com.community.dogcat.service.user.RefreshTokenService;
import com.community.dogcat.service.user.ReissueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class CustomSecurityConfig {

	private final JWTUtil jwtUtil;
	private final UserRepository userRepository;
	private final ReissueService reissueService;
	private final RefreshRepository refreshRepository;
	private final UsersAuthRepository usersAuthRepository;
	private final AuthenticationConfiguration configuration;
	private final CustomSuccessHandler customSuccessHandler;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	// 개선
	private final RefreshTokenService refreshTokenService;

	@Bean
	public BCryptPasswordEncoder cryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.exceptionHandling(exceptionHandling -> exceptionHandling
			.accessDeniedHandler(accessDeniedHandler));

		http.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable);

		http.authorizeRequests()
			.antMatchers("/admin/**").hasRole("ADMIN")
			.antMatchers("/user/**", "/", "/check/**", "/login/**", "/oauth2/**", "/error/**").permitAll()
			.antMatchers("/css/**", "/fonts/**", "/js/**", "/img/**", "/static/**", "/home/home").permitAll()
			.anyRequest().authenticated();

		http.formLogin()
			.loginPage("/user/login")
			.failureHandler(customAuthenticationFailureHandler)
			.permitAll();

		http.addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);
		http.addFilterBefore(new JWTFilter(jwtUtil, reissueService), LoginFilter.class);
		http.addFilterAt(
			new LoginFilter(authenticationManager(configuration), jwtUtil, refreshRepository, usersAuthRepository,
				userRepository, customAuthenticationFailureHandler),
			UsernamePasswordAuthenticationFilter.class);

		http.oauth2Login(oauth2 -> oauth2
			.failureHandler(customAuthenticationFailureHandler)
			.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
				.userService(customOAuth2UserService))
			.successHandler(customSuccessHandler)
		);

		http.sessionManagement(session -> session
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();

	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {

		return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());

	}

	@Bean
	public RequestContextListener requestContextListener() {

		return new RequestContextListener();

	}
}

