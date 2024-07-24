package com.community.dogcat.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.dto.board.reply.ReplyDTO;
import com.community.dogcat.service.board.reply.ReplyService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
public class ReplyServiceImplTests {

	@Autowired
	private ReplyService replyService;

	@Test
	public void testRegister() {
		ReplyDTO replyDTO = ReplyDTO.builder()
			.userId("user1")
			.postNo(20L)
			.replyContent("replyTest")
			.build();

		log.info(replyService.register(replyDTO));
	}

	@Test
	public void testRead() {
		Long replyNo = 2L;
		replyService.read(replyNo);
		log.info(replyNo);
	}

	// @Test
	// public void testDelete() {
	// 	Long replyNo = 2L;
	// 	replyService.delete(replyNo);
	// }

	// @Test
	// public void testGetListOfBoard() {
	// 	Long postNo = 10L;
	// 	// Post post = boardRepository.findById(postNo).orElse(null);
	// 	replyService.getListOfBoard(postNo);
	// 	log.info(postNo);
	// 	post.getContent().forEach(post -> log.info(post));
	// }
}


