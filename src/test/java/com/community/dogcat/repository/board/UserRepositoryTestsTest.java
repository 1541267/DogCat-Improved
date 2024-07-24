package com.community.dogcat.repository.board;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.domain.User;
import com.community.dogcat.repository.user.UserRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
class UserRepositoryTestsTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testInsert() {
		IntStream.rangeClosed(1, 5).forEach(i -> {
			User user = User.builder()
				.userId("user" + i)
				.userPw("pw" + i)
				.userName("userName" + i)
				.nickname("nickname" + i)
				.tel("000-0000-" + i)
				.build();

			User result = userRepository.save(user);
		});

		// User user = User.builder()
		// 	.userId("1")
		// 	.userPw("1")
		// 	.userName("userName")
		// 	.nickname("nickname")
		// 	.tel("000-0000-")
		// 	.build();
		// User result = userRepository.save(user);

	}

	@Test
	public void testFindByPostUser() {
		String userId = "user1";

		User user = userRepository.findById(userId).orElse(null);
		log.info(user.getNickname());
		log.info(user.getExp());
		log.info(user.isUserVet());
	}

	// @Test
	// public void findByNonBlockUsers() {
	// 	List<User> a = userRepository.findByBlockFalse();
	//
	// 	for (User b : a) {
	// 		log.info("userId: {} ", b.getUserId());
	// 	}
	//
	// }
}
