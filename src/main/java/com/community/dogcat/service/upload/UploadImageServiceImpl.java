package com.community.dogcat.service.upload;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnailator;

import com.amazonaws.services.s3.AmazonS3;
import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;
import com.community.dogcat.repository.upload.UploadRepository;
import com.community.dogcat.util.uploader.DeleteTempFiles;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadImageServiceImpl implements UploadImageService {

	// private final AmazonS3 amazonS3;
	private final UploadRepository uploadRepository;
	@Value("${baseUrl}")
	private String baseUrl;

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	@Value("${finalUploadPath}")
	private String finalUploadPath;

	// 최종 업로드 링크
	@Value("${newUrl}")
	private String finalUrl;

	// private final S3Uploader s3Uploader;

	// s3업로드 or 게시글 등록 취소 or 백스페이스 summernote 임시 업로드 이미지 파일 삭제
	// 개선, 놔두고 매일 새벽에 한꺼번에 정리 -> I/O 부담 제거
	private final DeleteTempFiles deleteTempFiles;

	// 개선, 레디스 템플릿
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	// 날짜로 파일 경로 분산
	private final String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

	// jsonArray
	// 썸머노트 업로드후 본문에 insert 하기위해 정보 반환
	@Override
	public String uploadSummerNoteImage(List<MultipartFile> multipartFiles, HttpServletRequest request) {

		JsonObject jsonObject = new JsonObject();
		JsonArray jsonArray = new JsonArray();

		// 이미지 저장 경로 설정
		String contextRoot = tempUploadPath;

		// 디렉토리가 없을경우 생성
		// File directory = new File(tempUploadPath);
		// 로컬 전용
		// File directory = new File(contextRoot);
		// if (!directory.exists()) {
		// 	directory.mkdirs();
		// }

		for (MultipartFile multipartFile : multipartFiles) {
			String originalFileName = multipartFile.getOriginalFilename();
			assert originalFileName != null;

			// 확장자 추출
			String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

			// 원본 파일 이름

			// UUID 파일명
			String uuid = UUID.randomUUID().toString();
			String saveFileName = uuid + extension;

			// 파일경로, 이름 정보를 저장하는 객체
			File targetFile = new File(contextRoot + saveFileName);

			try {
				InputStream fileStream = multipartFile.getInputStream();

				// 파일의 가로 세로 길이 정보 저장
				List<Integer> imagesLength = calcImageLength(multipartFile);

				FileUtils.copyInputStreamToFile(fileStream, targetFile);

				// 이미지의 URL 생성
				// String imageUrl = baseUrl + "/temp/" + saveFileName;
				// 아래는 로컬용
				String imageUrl = baseUrl + "/temp/" + saveFileName;
				JsonObject fileJsonObject = new JsonObject();

				// 생성된 파일의 uuid 와 이미지링크 summernote 에 전달
				fileJsonObject.addProperty("imageUrl", imageUrl);
				fileJsonObject.addProperty("uuid", uuid);
				fileJsonObject.addProperty("extension", extension);
				fileJsonObject.addProperty("name", originalFileName);
				fileJsonObject.addProperty("width", imagesLength.get(0));
				fileJsonObject.addProperty("height", imagesLength.get(1));

				jsonArray.add(fileJsonObject);

			} catch (IOException e) {
				// 파일 저장 중 오류가 발생한 경우 해당 파일 삭제 및 에러 응답 코드 추가
				log.error("Summernote Image Upload failed", e);
				FileUtils.deleteQuietly(targetFile);
				JsonObject errorJsonObject = new JsonObject();
				jsonObject.addProperty("responseCode", "error");
				jsonArray.add(errorJsonObject);
				e.printStackTrace();
			}

		}
		jsonObject.add("files", jsonArray);

		return jsonObject.toString();
	}

	@Override
	@Transactional
	// 개선, 게시글 등록 시 임시 파일을 업로드 성공 폴더로 옮김
	public void moveAndSaveImages(List<String> uuids, List<String> extensions, List<String> originalFileNames,
		Post postNo) throws IOException {
		// final 디렉터리 및 썸네일 디렉터리 보장

		Path finalDir = Paths.get(finalUploadPath + datePath);

		List<ImgBoard> imgs = new ArrayList<>();

		for (int i = 0; i < uuids.size(); i++) {
			String uuid = uuids.get(i);
			String ext = extensions.get(i);
			String saveFileName = uuid + ext;
			String originalFileName = originalFileNames.get(i);

			// 해싱 분산을 위한 uuid의 prefix 추출
			String prefix = uuid.substring(0, 2);

			Path thumbDir = finalDir.resolve(prefix).resolve("thumbnail");

			Files.createDirectories(thumbDir);

			// 임시 파일 경로
			Path tempFile = Paths.get(tempUploadPath, saveFileName);
			// 최종 파일 경로
			Path finalFile = finalDir.resolve(prefix).resolve(saveFileName);

			// 파일 이동 (복사 후 원본 삭제)
			Files.move(tempFile, finalFile);

			// 이동 후 썸네일 생성
			Path thumbFile = thumbDir.resolve("t_" + saveFileName);
			Thumbnailator.createThumbnail(finalFile.toFile(), thumbFile.toFile(), 200, 200);

			String finishedPath = finalUrl + datePath + "/" + prefix + "/";

			// DB 엔티티 저장
			ImgBoard img = ImgBoard.builder()
				.uploadPath(finishedPath + saveFileName)
				.thumbnailPath(finishedPath + "thumbnail/t_" + saveFileName)
				.deletePossible(false)
				.img(true)
				.uploadTime(Instant.now())
				.extension(ext)
				.fileName(originalFileName)
				.fileUuid(uuid)
				.postNo(postNo)
				.build();

			imgs.add(img);
			String MetaValue = finalFile + "|" + thumbFile;
			// key = "img~meta", hashKey = fileName, value = MetaValue
			redisTemplate.opsForHash().put("imgboard:meta", saveFileName, MetaValue);
		}
		// TODO 개선, 대량 INSERT의 batch 고려해보기
		uploadRepository.saveAll(imgs);
	}

	// 업로드된 이미지의 가로세로 정보 추출
	private List<Integer> calcImageLength(MultipartFile multipartFile) throws IOException {

		List<Integer> imagesLength = new ArrayList<>();

		try (InputStream inputStream = multipartFile.getInputStream()) {

			BufferedImage bufferedImage = ImageIO.read(inputStream);

			imagesLength.add(bufferedImage.getWidth());
			imagesLength.add(bufferedImage.getHeight());

		} catch (IOException e) {
			log.error("이미지 가로세로 추출 에러!");
			throw e;
		}
		return imagesLength;
	}

	// summernote 취소버튼 누를 때 임시파일 제거
	@Override
	public void deleteSummernoteImage(List<String> uuids, List<String> extensions) {

		for (int i = 0; i < uuids.size(); i++) {
			String fileName = uuids.get(i) + extensions.get(i);
			deleteTempFiles.deleteFile(fileName);
		}
	}

	@Override
	public void deleteSummernoteImageWithBackspace(List<String> deletedImageUrl) {

		for (String fileUrl : deletedImageUrl) {
			String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

			deleteTempFiles.deleteFile(fileName);
		}
	}

	// summernote 로 임시파일 업로드 후 게시글 등록하면 자동 s3 업로드
	// @Override
	// public ResponseEntity<List<String>> uploadS3Image(List<MultipartFile> multipartFile, Post postNo,
	// 	List<String> uuids) {
	//
	// 	List<String> error = new ArrayList<>();
	// 	List<String> uploadResult = new ArrayList<>();
	//
	// 	if (multipartFile == null || multipartFile.isEmpty()) {
	//
	// 		log.error("업로드된 파일 없음!");
	//
	// 		error.add("업로드된 파일이 없음!");
	// 		error.add(String.valueOf(postNo));
	// 		error.add(String.valueOf(System.currentTimeMillis()));
	//
	// 		return ResponseEntity.badRequest().body(error);
	// 	}
	//
	// 	for (int i = 0; i < uuids.size(); i++) {
	//
	// 		MultipartFile file = multipartFile.get(i);
	//
	// 		String originalFileName = file.getOriginalFilename();
	// 		String fileUuid = uuids.get(i);
	//
	// 		try {
	// 			// s3업로드 실행 전에 게시글 등록 로직 먼저 실행 후 반환 되는 postNo
	// 			uploadResult = s3Uploader.upload(file, postNo, fileUuid);
	//
	// 		} catch (Exception e) {
	// 			log.error("S3 업로드 에러", e);
	// 			error.add("S3 업로드 에러: " + e.getMessage());
	// 			error.add(String.valueOf(postNo));
	// 			error.add(originalFileName);
	// 			error.add(fileUuid);
	// 			error.add(String.valueOf(System.currentTimeMillis()));
	// 			return ResponseEntity.status(500).body(error);
	// 		}
	// 	}
	//
	// 	return ResponseEntity.ok(uploadResult);
	// }
	//
	// @Override
	// @Transactional
	// public void deleteUploadedS3Image(List<String> deletedImageUrls) {
	// 	// 버킷의 업로드된 파일 제거
	// 	for (String imageUrl : deletedImageUrls) {
	// 		s3Uploader.deleteS3BucketFile(imageUrl);
	// 		uploadRepository.deleteByUploadPath(imageUrl);
	// 	}
	// }
}