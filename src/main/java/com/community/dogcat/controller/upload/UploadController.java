package com.community.dogcat.controller.upload;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.community.dogcat.annotation.MultipartParam;
import com.community.dogcat.domain.Post;
import com.community.dogcat.dto.uploadImage.UploadPostImageDTO;
import com.community.dogcat.dto.uploadImage.UploadPostImageResultDTO;
import com.community.dogcat.service.upload.UploadImageService;
import com.community.dogcat.util.uploader.S3LocalUploader;
import com.community.dogcat.util.uploader.S3Uploader;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class UploadController {

	private final UploadImageService uploadImageService;

	// S3 업로드
	@Operation(summary = "Upload S3", description = "S3 업로드")
	@PostMapping(value = "/s3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<List<String>> cloudUpload(
		@RequestParam("files") List<MultipartFile> multipartFile,
		@RequestParam("postNo") Post postNo,
		@RequestParam("uuids") List<String> uuid) {

		return uploadImageService.uploadS3Image(multipartFile, postNo, uuid);
	}

	// 게시글 등록 전 썸머노트로 이미지를 임시폴더에 저장
	@Operation(summary = "SummerNote Image Upload", description = "썸머노트 임시 이미지 업로드")
	@PostMapping(value = "/summernote-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadSummerNoteImage(@RequestParam("files") List<MultipartFile> multipartFile,
		HttpServletRequest request) throws IOException {

		return uploadImageService.uploadSummerNoteImage(multipartFile, request);
	}

	// 게시글 취소, 브라우저 종료시 업로드된 임시파일 삭제
	@PostMapping("/delete-temp")
	public void deleteSummernoteTempFile(@RequestParam("uuid") List<String> uuids,
		@RequestParam("extension") List<String> extensions) {

		uploadImageService.deleteSummernoteImage(uuids, extensions);
	}

	// summernote 본문의 이미지를 제거시 임시파일 삭제
	@PostMapping("/delete-backspace")
	public void deleteSummernoteTempFileWithBackspace(
		@RequestParam("deletedImageUrl") List<String> deletedImageUrl) {

		uploadImageService.deleteSummernoteImageWithBackspace(deletedImageUrl);

	}
}
