package com.community.dogcat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.repository.board.reply.ReplyRepository;

@SpringBootTest
public class ReplyRepositoryTest {

	@Autowired
	private ReplyRepository replyRepository;

	@Test
	public void findReplyCount() {

		System.out.println("replyRepository.cout = " + replyRepository.countByPostNo(51169L));
	}
}


