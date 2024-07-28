package com.community.dogcat.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class UploadRepositoryTest {

	@Autowired
	private UploadRepository uploadRepository;

	@Autowired
	private BoardRepository boardRepository;

	@Test
	public void upload() {

		Optional<Post> optionalPost = boardRepository.findById(3L);

		if (optionalPost.isPresent()) {
			Post post = optionalPost.get();
			for (int i = 1; i < 5; i++) {
				ImgBoard result = ImgBoard.builder()
					.fileUuid(String.valueOf(UUID.randomUUID()))
					.postNo(post)
					.fileName("646" + ".jpg")
					.uploadTime(Instant.now())
					.build();

				uploadRepository.save(result);
			}
		}
	}

	@Test
	public void deleteImage() {
		String imageId = "be1f9144-253e-419b-9608-f2b39430cbe7";

		uploadRepository.deleteById(imageId);

		Optional<ImgBoard> findImage = uploadRepository.findById(imageId);
		if (findImage.isEmpty()) {
			log.info("파일 삭제 성공!");
		} else {
			log.info("파일 삭제 에러!");
		}
	}

}
