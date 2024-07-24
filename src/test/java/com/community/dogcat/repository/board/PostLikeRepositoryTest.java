package com.community.dogcat.repository.board;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.PostLike;
import com.community.dogcat.domain.User;
import com.community.dogcat.repository.board.postLike.PostLikeRepository;
import com.community.dogcat.repository.user.UserRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
public class PostLikeRepositoryTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private PostLikeRepository postLikeRepository;

	@Test
	public void register() {
		User user = userRepository.findById("1234@1234.com").orElse(null);
		Post post = boardRepository.findById(2L).orElse(null);
		if (user != null && post != null) {
			PostLike postLike = PostLike.builder()
				.postNo(post)
				.userId(user)
				.likeState(false)
				.dislikeState(true)
				// 	// .recommendState(true)
				// 	// .objectionState(false)
				.build();

			PostLike result = postLikeRepository.save(postLike);
			log.info(result);
		}
	}

	// @Test
	// public void delete() {
	// 	Long likeNo = 28L;
	// 	postLikeRepository.delete(2L);
	// }
}
