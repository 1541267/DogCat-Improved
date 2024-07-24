package com.community.dogcat.repository.board;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.User;
import com.community.dogcat.repository.user.UserRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
class BoardRepositoryTest {

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testInsert() {
		IntStream.rangeClosed(1, 20).forEach(i -> {

			User user = userRepository.findById("1").orElse(null);
			if (user != null) {
				Post post = Post.builder()
					.userId(user)
					.boardCode("tip")
					.postTitle("postTitle" + i)
					.postContent("postContent" + i)
					.postTag("고양이")
					.build();

				Post result = boardRepository.save(post);
				log.info("postNo: " + result.getPostNo());
			} else {
				log.warn("User not found with user1");
			}
		});
	}

	@Test
	public void testInsert2() {
		IntStream.rangeClosed(1, 15).forEach(i -> {
			User user = userRepository.findById("user1").orElse(null);
			if (user != null) {
				Post post = Post.builder()
					.userId(user)
					.postTitle("postTitle" + i)
					.postContent("postContent" + i)
					.build();

				// log.info("postNo: " + result.getPostNo()); ?
			}
		});
	}

	@Test
	public void testReadOne() {
		Long postNo = 2L;

		Optional<Post> result = boardRepository.findById(postNo);
		Post post = result.orElse(null);
		log.info(post.getPostNo());
	}

	@Test
	public void testUpdate() {
		Long postNo = 2L;

		Optional<Post> result = boardRepository.findById(postNo);
		Post post = result.orElse(null);
		post.modify("general", "updateTitle", "updateContent", Instant.now(), "고양이", true, true);
		boardRepository.save(post);
		log.info(post.getBoardCode());
		log.info(post.getPostTitle());
		log.info(post.getPostContent());
		log.info(post.getModDate());
		log.info(post.getPostTag());
		log.info(post.isSecret());
		log.info(post.isReplyAuth());
	}

	@Test
	public void testDelete() {
		Long postNo = 2L;
		boardRepository.deleteById(postNo);
	}

	@Test
	public void testPaging() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("postNo").descending());
		Page<Post> result = boardRepository.findAll(pageable);
		log.info(result.getTotalElements());
		log.info(result.getTotalPages());
		log.info(result.getNumber());
		log.info(result.getSize());

		List<Post> list = result.getContent();
		list.forEach(post -> log.info(post));
	}

	// @Test
	// public void testList() {
	// 	String[] types = {"t", "c", "u"};
	// 	String keyword = "1";
	// 	Pageable pageable = PageRequest.of(0, 10, Sort.by("postNo").descending());
	// 	Page<Post> result = boardRepository.searchAll(types, keyword, pageable);
	// 	log.info(result.getTotalPages());
	// 	log.info(result.getSize());
	// 	log.info(result.getNumber());
	// 	log.info(result.hasPrevious() + "," + result.hasNext());
	// 	result.getContent().forEach(post -> log.info(post));
	// }

	// @Test
	// public void testList() {
	// 	String[] types = {"t", "c", "u"};
	// 	String keyword = "1";
	// 	Pageable pageable = PageRequest.of(0, 10, Sort.by("postNo").descending());
	// 	String boardCode = "general";
	// 	Page<BoardListDTO> result = boardRepository.listWithAll(types, keyword, pageable, boardCode);
	// 	log.info(result.getTotalPages());
	// 	log.info(result.getSize());
	// 	log.info(result.getNumber());
	// 	log.info(result.hasPrevious() + "," + result.hasNext());
	// 	result.getContent().forEach(post -> log.info(post));
	// }

	// @Test
	// void findPostTest() {
	// 	String userId = "user1";
	// 	List<Post> post = boardRepository.findByUserId(userId);
	// 	log.info("{}", post);
	// }
}
