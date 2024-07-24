package com.community.dogcat.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
public class PostServiceTest {

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private UploadRepository uploadRepository;

	@Test
	public void findImgWithPost() {
		//파일 이름으로 찾기
		// String uuid = "0c049bd4-d8e0-4953-8c57-5f39f9696e01";

		// log.info("-----------------------------" + boardRepository.findById(
		// 		uploadRepository.findById(uuid).get().getPostNo().getPostNo())
		// 	.get().getImages());

		// Optional<ImgBoard> a = uploadRepository.findById(uuid);
		// Optional<Post> b = boardRepository.findById(a.get().getPostNo().getPostNo());
		// log.info("---------------------" + b.get().getPostNo());

		//postNo 으로 찾기

		Long postId = 1L;

		Optional<Post> postImage = boardRepository.findById(postId);

		if (postImage.isPresent()) {
			List<ImgBoard> images = postImage.get().getImages();
			for (ImgBoard img : images) {
				log.info("File UUID: " + img.getFileUuid());
			}

		} else {
			log.warn("Post with id " + postId + " not found.");
		}
	}
}
