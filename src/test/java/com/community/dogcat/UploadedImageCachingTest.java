package com.community.dogcat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;

@SpringBootTest
public class UploadedImageCachingTest {

	@Autowired
	private UploadRepository uploadRepository;

	private static final String REDIS_KEY = "imgboard:meta";

	@Test
	void preloadCache() {

		Set<FileInfoDTO> files = uploadRepository.findAllFileUuidAndExtensionAndUploadTimeAndDeletePossible();
		for (FileInfoDTO file : files) {
			System.out.println(REDIS_KEY + "\n" + file.getUploadPath() + "\n" + file.getUploadThumbPath());
		}

	}
}
