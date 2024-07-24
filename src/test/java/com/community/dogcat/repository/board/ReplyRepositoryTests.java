package com.community.dogcat.repository.board;

import java.util.stream.IntStream;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.domain.User;
import com.community.dogcat.repository.board.reply.ReplyRepository;
import com.community.dogcat.repository.user.UserRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
public class ReplyRepositoryTests {

	@Autowired
	private ReplyRepository replyRepository;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testInsert() {
		IntStream.rangeClosed(1, 5).forEach(i -> {
			User user = userRepository.findById("1234@1234.com").orElse(null);
			Post post = boardRepository.findById(3L).orElse(null);
			if (user != null && post != null) {
				Reply reply = Reply.builder()
					.userId(user)
					.postNo(post)
					.replyContent("replyContent" + i)
					.build();

				Reply result = replyRepository.save(reply);
				log.info(result);
			}
		});
	}

	@Test
	public void testRead() {
		Post postNo = boardRepository.findById(17L).orElse(null);
		if (postNo != null) {
			Pageable pageable = PageRequest.of(0, 10, Sort.by("replyNo").descending());
			Page<Reply> result = replyRepository.listOfBoard(postNo, pageable);
			result.getContent().forEach(reply -> {
				log.info("ReplyNo: {}", reply.getReplyNo());
				log.info("ReplyContent: {}", reply.getReplyContent());
				log.info("ReplyRegDate: {}", reply.getRegDate());
				log.info("ReplyRegDate GetClass: {}", reply.getRegDate().getClass());
				// log.info(reply.getUserId());
				// log.info(reply.getPostNo());
			});
		}
	}

	@Test
	@Transactional
	@Commit
	public void testRemoveReply() {
		Long replyNo = 3L;

		Reply reply = replyRepository.findById(replyNo).orElse(null);
		if (reply != null) {
			log.info("Deleting reply: " + reply);
			replyRepository.deleteById(replyNo);
		} else {
			log.warn("Reply not found with replyNo: " + replyNo);
		}

		Reply deletedReply = replyRepository.findById(replyNo).orElse(null);
		assert deletedReply == null : "Reply was not deleted.";
		log.info("Reply successfully deleted with replyNo: " + replyNo);
	}

	@Test
	public void testCountRepliesByPostNo() {
		Long postNo = 15L;
		Post post = boardRepository.findById(postNo).orElseThrow();
		log.info(replyRepository.countRepliesByPostNo(post));
	}
}
