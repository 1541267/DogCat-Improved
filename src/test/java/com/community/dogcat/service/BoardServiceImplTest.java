package com.community.dogcat.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.User;
import com.community.dogcat.dto.board.BoardListDTO;
import com.community.dogcat.dto.board.BoardPageRequestDTO;
import com.community.dogcat.dto.board.BoardPageResponseDTO;
import com.community.dogcat.dto.board.post.PostDTO;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.service.board.BoardService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
class BoardServiceImplTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BoardService boardService;

	// @Test
	// public void testRegister() {
	// 	User users = userRepository.findById("user1").orElse(null);
	// 	if (users != null) {
	// 		PostDTO postDTO = PostDTO.builder()
	// 			.userId(users)
	// 			.boardCode((byte)2)
	// 			.postTitle("postTitle")
	// 			.postContent("postContent")
	// 			.build();
	//
	// 		String user = users.getUserId();
	// 		log.info("---------------" + user);
	//
	// 		log.info(boardService.register(user, postDTO));
	// 	}
	// }

	// @Test
	// public void register() {
	//
	// 	UserPostDTO userPostDTO = UserPostDTO.builder()
	// 		.userId("user1")
	// 		.build();
	// 	log.info(userPostDTO);
	//
	// 	User user = userRepository.findById("user1")
	// 		.orElse(null);
	// 	log.info(user);
	//
	// 	if (user != null) {
	// 		PostDTO postDTO = PostDTO.builder()
	// 			.userId(userPostDTO)
	// 			.boardCode((byte)3)
	// 			.objectionCount(8L)
	// 			.postContent("ServiceTest")
	// 			.postTag("고양이")
	// 			.postTitle("ServiceTest")
	// 			.recommendCount(3L)
	// 			.replyAuth(true)
	// 			.secret(true)
	// 			.viewCount(1L)
	// 			.build();
	// 		log.info(postDTO);
	//
	// 		User users = userRepository.findById("user1")
	// 			.orElse(null);
	// 		log.info(user);
	//
	// 		if (users != null) {
	// 			Post post = Post.builder()
	// 				.userId(users)
	// 				.boardCode(postDTO.getBoardCode())
	// 				.objectionCount(postDTO.getObjectionCount())
	// 				.postContent(postDTO.getPostContent())
	// 				.postTag(postDTO.getPostTag())
	// 				.postTitle(postDTO.getPostTitle())
	// 				.recommendCount(postDTO.getRecommendCount())
	// 				.replyAuth(postDTO.getReplyAuth())
	// 				.secret(postDTO.getSecret())
	// 				.viewCount(postDTO.getViewCount())
	// 				.build();
	// 			log.info(post);
	//
	// 			boardService.register(postDTO);
	// 		}
	// 	}
	// }

	@Test
	public void register() {

		String userId = "user1";
		log.info(userId);

		PostDTO postDTO = PostDTO.builder()
			.userId(userId)
			.boardCode("general")
			.dislikeCount(8L)
			.postContent("ServiceTest")
			.postTag("고양이")
			.postTitle("ServiceTest")
			.likeCount(3L)
			.replyAuth(true)
			.secret(true)
			.viewCount(1L)
			.build();
		log.info(postDTO);

		User user = userRepository.findById("user1")
			.orElse(null);
		log.info(user);

		if (user != null) {

			Post post = Post.builder()
				.userId(user)
				.boardCode(postDTO.getBoardCode())
				.dislikeCount(postDTO.getDislikeCount())
				.postContent(postDTO.getPostContent())
				.postTag(postDTO.getPostTag())
				.postTitle(postDTO.getPostTitle())
				.likeCount(postDTO.getLikeCount())
				.replyAuth(postDTO.isReplyAuth())
				.secret(postDTO.isSecret())
				.viewCount(postDTO.getViewCount())
				.build();
			log.info(post);

			boardService.register(postDTO);
		}
	}

	// @Test
	// void delete() {
	// 	Long postNo = 11L;
	// 	boardService.delete(postNo);
	// 	log.info(postNo);
	// }

	@Test
	void readOne() {
		Long postNo = 2L;
		boardService.readOne(postNo);
		log.info(postNo);
	}

	// @Test
	// void Modify() {
	// 	Long postNo = 34L;
	//
	// 	PostDTO postDTO = PostDTO.builder()
	// 		.postNo(postNo)
	// 		.userId("user1")
	// 		.boardCode("general")
	// 		.dislikeCount(8L)
	// 		.postContent("ServiceTest")
	// 		.postTag("고양이")
	// 		.postTitle("ServiceTest")
	// 		.likeCount(3L)
	// 		.replyAuth(true)
	// 		.secret(true)
	// 		.viewCount(1L)
	// 		.build();
	// 	log.info(postDTO);
	//
	// 	boardService.modify(postDTO);
	// }

	@Test
	public void testList() {
		BoardPageRequestDTO pageRequestDTO = BoardPageRequestDTO.builder()
			.type("tcw")
			.keyword("1")
			.page(1)
			.size(10)
			.build();

		BoardPageResponseDTO<BoardListDTO> responseDTO = boardService.list(pageRequestDTO);
		log.info(responseDTO);
	}
}
