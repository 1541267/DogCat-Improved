package com.community.dogcat.service;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.community.dogcat.domain.User;
import com.community.dogcat.domain.UsersAuth;
import com.community.dogcat.dto.user.JoinDTO;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
class JoinServiceTest {

	@Autowired
	public UserRepository userRepository;

	@Autowired
	public UsersAuthRepository usersAuthRepository;

	@Autowired
	public BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	public ModelMapper modelMapper;

	@Test
	public void joinTest() {

		JoinDTO joinDTO = JoinDTO.builder()
			.userId("seulgi0587@naver.com")
			.userPw(bCryptPasswordEncoder.encode("1111"))
			.userName("김슬기")
			.nickname("슬바오")
			.tel("010-0000-0000")
			.userVet(false)
			.build();

		User user = modelMapper.map(joinDTO, User.class);

		userRepository.save(user);

		UsersAuth usersAuth = UsersAuth.builder()
			.userId(user.getUserId())
			.build();
		usersAuthRepository.save(usersAuth);
	}

	@Test
	public void createMultipleUsers() {
		int numberOfUsers = 50; // 생성할 유저 수

		for (int i = 3; i < numberOfUsers; i++) {
			JoinDTO joinDTO = JoinDTO.builder()
				.userId("user" + i + "@example.com")
				.userPw(bCryptPasswordEncoder.encode("password" + i))
				.userName("사용자" + i)
				.nickname("닉네임" + i)
				.tel("010-0000-0000")
				.userVet(false)
				.build();

			User user = modelMapper.map(joinDTO, User.class);

			userRepository.save(user);

			UsersAuth usersAuth = UsersAuth.builder()
				.userId(user.getUserId())
				.build();
			usersAuthRepository.save(usersAuth);
		}
	}

}
