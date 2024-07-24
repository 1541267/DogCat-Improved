package com.community.dogcat.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.dto.board.postLike.PostLikeDTO;
import com.community.dogcat.service.board.postLike.PostLikeService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
public class PostLikeServiceTest {

	@Autowired
	private PostLikeService postLikeService;

	@Test
	public void register() {
		PostLikeDTO postReadDTO = PostLikeDTO.builder()
			.postNo(246L)
			.userId("user1")
			.likeState(false)
			.dislikeState(true)
			// .recommendState(true)
			// .objectionState(false)

			.likeCount(null)
			.dislikeCount(null)
			.build();

		log.info(postLikeService.register(postReadDTO));
		log.info(postReadDTO.getDislikeCount());
		log.info(postReadDTO.getLikeCount());
	}

	// @Test
	// public void delete() {
	// 	Long likeNo = 28L;
	// 	postLikeService.delete(likeNo);
	// }
}
