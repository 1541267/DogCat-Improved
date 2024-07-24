package com.community.dogcat.repository.board;

import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Scrap;
import com.community.dogcat.domain.User;
import com.community.dogcat.repository.board.scrap.ScrapRepository;
import com.community.dogcat.repository.user.UserRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
class ScrapRepositoryTest {

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ScrapRepository scrapRepository;

	@Test
	public void testInsert() {
		IntStream.rangeClosed(1, 1).forEach(i -> {
			User user = userRepository.findById("1234@1234.com").orElse(null);
			Post post = boardRepository.findById(3L).orElse(null);
			if (user != null && post != null) {
				Scrap scrap = Scrap.builder()
					.userId(user)
					.postNo(post)
					.build();

				Scrap result = scrapRepository.save(scrap);
				log.info("scrapNo: " + result.getScrapNo());
			}
		});
	}

	@Test
	public void testReadOne() {
		Long scrapNo = 5L;
		Optional<Scrap> result = scrapRepository.findById(scrapNo);
		Scrap scrap = result.orElseThrow();
		log.info(scrap);
	}

	@Test
	public void testDelete() {
		Long scrapNo = 1L;
		scrapRepository.deleteById(scrapNo);
		log.info(scrapNo);
	}
}