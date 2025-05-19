package com.community.dogcat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;

@SpringBootTest
public class TestUploadRepository {

	@Autowired
	UploadRepository uploadRepository;

	@Test
	public void testFindFileUuidAndExtensionByDeletePossibleTrue() {

		for (FileInfoDTO i : uploadRepository.findFileUuidAndExtensionByDeletePossibleTrue()) {
			System.out.println("==========================================================");
			System.out.println(i.getFullName());
		}

	}
}
