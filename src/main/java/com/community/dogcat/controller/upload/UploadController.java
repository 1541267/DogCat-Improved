package com.community.dogcat.controller.upload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import com.community.dogcat.util.uploader.DeleteTempFiles;

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
	// private final UploadRepository uploadRepository;
	// 로컬 전용, 섬네일+이미지의 url
	// @Value("${baseUrl}")
	// private String baseUrl;

	// 최종 업로드 링크
	// @Value("${newUrl}")
	// private String finalUrl;
	// 최종 업로드 경로
	// @Value("${finalUploadPath}")
	// private String finalUploadPath;

	// 개선, 지우지 않고 옮김 |업로드 완료 후 임시파일 제거
	// private final DeleteTempFiles deleteTempFiles;

	// @Value("${tempUploadPath}")
	// private String tempUploadPath;

	@Operation(summary = "SummerNote Final Image Upload", description = "썸머노트 이미지 업로드")
	@PostMapping(value = "/finalImageUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	// 로컬 전용 게시글 등록시 임시 파일을 이동해서 업로드 완료,
	// s3 사용 안하고 이것만 사용
	public ResponseEntity<List<String>> finalImageUpload(
		// @RequestParam("files") List<MultipartFile> files,
		@RequestParam("postNo") Post postNo,
		@RequestParam("uuids") List<String> uuids,
		@RequestParam("extensions") List<String> extensions,
		@RequestParam("originalFileNames") List<String> originalFileNames
	) throws IOException {

		List<String> errors = new ArrayList<>();
		// List<String> fileNames = new ArrayList<>();

		uploadImageService.moveAndSaveImages(uuids, extensions, originalFileNames, postNo);

		// for (int i = 0; i < files.size(); i++) {
		// 	MultipartFile mf = files.get(i);
		// 	String uuid = uuids.get(i);
		//
		// 	try {
		// 		String fileName = Objects.requireNonNull(mf.getOriginalFilename());
		// 		String extension = fileName.substring(fileName.lastIndexOf("."));
		// 		boolean isImg = List.of(".png", ".jpg", ".jpeg", ".gif")
		// 			.contains(extension.toLowerCase());
		//
		// 		Path savePath = Paths.get(finalUploadPath, uuid + extension);
		// 		Path thumbnailDir = Paths.get(finalUploadPath + "thumbnail/");
		//
		// 		// 개선, 파일 업로드 매 용청 마다 호출로 인한 성능 저하
		// 		// createDirs(savePath);
		// 		// createDirs(thumbnailDir);
		//
		// 		System.out.println(
		// 			"finalUpload savePath : " + savePath + ", fileName : " + fileName + ", thumbnailDir :"
		// 				+ thumbnailDir + "/t_"
		// 				+ uuid + extension);
		//
		// 		// 파일 저장
		// 		mf.transferTo(savePath.toFile());
		//
		// 		// 썸네일 생성
		// 		File thumbFile = thumbnailDir.resolve("t_" + uuid + extension).toFile();
		// 		Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200, 200);
		//
		// 		// DB 저장
		// 		ImgBoard img = ImgBoard.builder()
		// 			.uploadPath(finalUrl + uuid + extension)
		// 			.thumbnailPath(finalUrl + "thumbnail/t_" + uuid + extension)
		// 			// .thumbnailPath(thumbnailDir + uuid + extension)
		// 			.img(isImg)
		// 			.uploadTime(Instant.now())
		// 			.extension(extension)
		// 			.fileName(fileName)
		// 			.fileUuid(uuid)
		// 			.postNo(postNo)
		// 			.build();
		//
		// 		uploadRepository.save(img);
		//
		// 		fileNames.add(uuid + extension);
		//
		// 	} catch (Exception e) {
		// 		log.error("업로드 에러 (idx={}, uuid={}): {}", i, uuid, e.getMessage(), e);
		// 		errors.add(String.format("idx=%d, uuid=%s, err=%s", i, uuid, e.getMessage()));
		// 	}

		//  // 로컬 전환으로 인한 임시파일 제거
		//	// deleteTempFiles.deleteFileAfterUpload(fileNames);
		// }

		// 모든 파일 처리 후 한 번만 리턴
		return ResponseEntity.ok(errors);
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

	// 개선, 파일 업로드 매 용청 마다 호출로 인한 성능 저하
	// private void createDirs(Path dir) throws IOException {
	// 	if (Files.notExists(dir)) {
	// 		Files.createDirectories(dir);
	// 	}
	// }

	// S3 업로드
	// @Operation(summary = "Upload S3", description = "S3 업로드")
	// @PostMapping(value = "/s3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	// public ResponseEntity<List<String>> cloudUpload(
	// 	@RequestParam("files") List<MultipartFile> multipartFile,
	// 	@RequestParam("postNo") Post postNo,
	// 	@RequestParam("uuids") List<String> uuid) {
	//
	// 	return uploadImageService.uploadS3Image(multipartFile, postNo, uuid);
	// }
	//
	// @PostMapping("/delete-s3file")
	// public void deleteS3FileWithBucket(@RequestParam("deletedS3ImageUrls") List<String> deletedImageUrls) {
	//
	// 	uploadImageService.deleteUploadedS3Image(deletedImageUrls);
	//
	// }
}
