package com.community.dogcat.service.user;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.community.dogcat.domain.User;
import com.community.dogcat.domain.UsersAuth;
import com.community.dogcat.dto.social.GoogleResponse;
import com.community.dogcat.dto.social.KakaoResponse;
import com.community.dogcat.dto.social.NaverResponse;
import com.community.dogcat.dto.social.OAuth2Response;
import com.community.dogcat.dto.user.CustomOAuth2User;
import com.community.dogcat.dto.user.UserDTO;
import com.community.dogcat.oauth2.CustomOAuth2Exception;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final UsersAuthRepository usersAuthRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		OAuth2Response oAuth2Response = null;

		switch (registrationId) {
			case "naver" -> oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
			case "google" -> oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
			case "kakao" -> oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
			default -> {
				return null;
			}
		}

		String userId = oAuth2Response.getEmail();
		User existData = userRepository.findByUserId(userId);

		if (existData == null) {

			User user = User.builder()
				.userId(oAuth2Response.getEmail())
				.userPw("-")
				.userName(oAuth2Response.getName())
				.nickname(registrationId + "_" + oAuth2Response.getName())
				.social(true)
				.tel("-")
				.exp(0L)
				.build();

			userRepository.save(user);

			UsersAuth usersAuth = UsersAuth.builder()
				.userId(user.getUserId())
				.build();
			usersAuthRepository.save(usersAuth);

			UserDTO userDTO = UserDTO.builder()
				.userId(oAuth2Response.getEmail())
				.userName(oAuth2Response.getName())
				.role("ROLE_USER")
				.build();

			return new CustomOAuth2User(userDTO);

		} else {
			if (!existData.isSocial()) {
				// 일반 회원이 이미 존재하는 경우 소셜 회원가입을 막음
				throw new CustomOAuth2Exception("해당 이메일로 이미 회원가입이 되어 있습니다.\\n일반 로그인을 시도해 주세요.");
			}

			if (existData.isSocial() && !existData.getNickname().startsWith(registrationId + "_")) {
				throw new CustomOAuth2Exception("동일한 이메일 ID가 다른 소셜 제공자에 의해 사용되고 있습니다.");
			}

			if (existData.isBlock()) {
				throw new CustomOAuth2Exception("관리자에 의해 계정이 차단되었습니다.");
			}

			UsersAuth usersAuth = UsersAuth.builder()
				.userId(existData.getUserId())
				.build();

			UserDTO userDTO = UserDTO.builder()
				.userId(oAuth2Response.getEmail())
				.userName(oAuth2Response.getName())
				.role(usersAuth.getAuthorities())
				.build();

			return new CustomOAuth2User(userDTO);
		}
	}
}
