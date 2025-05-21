
package com.community.dogcat.service.upload;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.community.dogcat.domain.Post;

@Service
public interface UploadImageService {

	String uploadSummerNoteImage(List<MultipartFile> multipartFile, HttpServletRequest request) throws IOException;

	void moveAndSaveImages(List<String> uuids, List<String> extensions, List<String> originalFileNames,
		Post postNo) throws IOException;

	void deleteSummernoteImageWithBackspace(List<String> deletedImageUrls);

	void deleteSummernoteImage(List<String> uuids, List<String> extensions);
	// void deleteUploadedS3Image(List<String> deletedImageUrls);
	// ResponseEntity<List<String>> uploadS3Image(List<MultipartFile> multipartFile, Post postNo,
	// 	List<String> uuid);
}
