package com.community.dogcat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;

@SpringBootTest
public class TestUploadRepository {

	@Autowired
	UploadRepository uploadRepository;

	@Test
	public void testFindFileUuidAndExtensionAndUploadTimeAndDeletePossibleByDeletePossibleTrue() {

		for (FileInfoDTO i : uploadRepository.findFileUuidAndExtensionAndUploadTimeAndDeletePossibleByDeletePossibleTrue()) {
			System.out.println("==========================================================");
			System.out.println(i.getFullName());
		}

	}
}
