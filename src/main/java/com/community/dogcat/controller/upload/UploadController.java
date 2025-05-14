package com.community.dogcat.controller.upload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnailator;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;
import com.community.dogcat.repository.upload.UploadRepository;
import com.community.dogcat.service.upload.UploadImageService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
public class UploadController {

	private final UploadImageService uploadImageService;

	// 로컬 전용 위한 summernoteUpload 25/02/06 추가
	private final UploadRepository uploadRepository;

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	// @Value("${uploadPath}")
	private String finalUploadPath = "http://localhost:10000/temp/thumbnail/";

	@Operation(summary = "SummerNote Final Image Upload", description = "썸머노트 이미지 업로드")
	@PostMapping(value = "/finalImageUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

	// 로컬 전용 업로드, s3 사용 안하고 이것만 사용
	public ResponseEntity<List<String>> finalImageUpload(String uploadPath,
		String thumbnailPath, boolean isImg, String extension, String fileName,
		@RequestParam("files") List<MultipartFile> multipartFile,
		@RequestParam("postNo") Post postNo,
		@RequestParam("uuids") List<String> uuids) throws IOException {

		for (int i = 0; i < multipartFile.size(); i++) {

			System.out.println("multipartFile = " + multipartFile);
			System.out.println("uuids = " + uuids);

			fileName = multipartFile.get(i).getOriginalFilename();
			String uuid = uuids.get(i);

			assert fileName != null;
			extension = fileName.substring(fileName.lastIndexOf("."));

			uploadPath = finalUploadPath + uuid + extension;
			String thumbnailFile = tempUploadPath + "t_" + uuid + extension;

			if (extension.equals(".png") || extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(
				".gif")) {
				isImg = true;
			}

			Path finalUploadPath = Paths.get(tempUploadPath, uuid + extension);

			System.out.println("finalUploadPath = " + finalUploadPath);
			System.out.println("finalUploadPath thumbnail = " + thumbnailPath);

			File thumbFile = new File(thumbnailPath);

			if (!thumbFile.exists()) {
				thumbFile.mkdirs();
			}

			Thumbnailator.createThumbnail(finalUploadPath.toFile(), thumbFile, 200, 200);

			System.out.println("uploadPath = " + uploadPath);
			System.out.println("thumbnailPath = " + thumbnailPath);
			System.out.println("isImg = " + isImg);
			System.out.println("extension = " + extension);
			System.out.println("fileName = " + fileName);
			System.out.println("uuid = " + uuid);
			System.out.println("postNo = " + postNo.getPostNo());
			System.out.println("postTitle = " + postNo.getPostTitle());

			ImgBoard result = ImgBoard.builder()
				.uploadPath(uploadPath)
				.thumbnailPath(thumbnailPath)
				.img(isImg)
				.uploadTime(Instant.now())
				.extension(extension)
				.fileName(fileName)
				.fileUuid(uuid)
				.postNo(postNo)
				.build();
			List<String> error = new ArrayList<>();

			try {
				uploadRepository.save(result);

			} catch (Exception e) {

				log.error("업로드 에러", e);
				error.add("업로드 에러: " + e.getMessage());
				error.add(String.valueOf(postNo));
				error.add(fileName);
				error.add(uuid);
				error.add(String.valueOf(System.currentTimeMillis()));
				return ResponseEntity.status(500).body(error);
			}
			return ResponseEntity.ok(error);
		}
		return null;

	}

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
		@RequestParam("deletedImageUrls") List<String> deletedImageUrls) {

		uploadImageService.deleteSummernoteImageWithBackspace(deletedImageUrls);

	}

	@PostMapping("/delete-s3file")
	public void deleteS3FileWithBucket(@RequestParam("deletedS3ImageUrls") List<String> deletedImageUrls) {

		uploadImageService.deleteUploadedS3Image(deletedImageUrls);

	}
}
