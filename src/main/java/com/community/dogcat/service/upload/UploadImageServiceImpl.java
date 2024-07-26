package com.community.dogcat.service.upload;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.community.dogcat.domain.Post;
import com.community.dogcat.util.uploader.DeleteTempFiles;
import com.community.dogcat.util.uploader.S3Uploader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class UploadImageServiceImpl implements UploadImageService {

	// TODO
	// @Value("${baseUrl}")
	// private String baseUrl;

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	private final S3Uploader s3Uploader;

	// s3업로드 or 게시글 등록 취소 or 백스페이스 summernote 임시 업로드 이미지 파일 삭제
	private final DeleteTempFiles deleteTempFiles;

	// jsonArray
	// 썸머노트 업로드후 본문에 insert 하기위해 정보 반환
	@Override
	public String uploadSummerNoteImage(List<MultipartFile> multipartFiles, HttpServletRequest request) throws
		IOException {

		JsonObject jsonObject = new JsonObject();
		JsonArray jsonArray = new JsonArray();

		// 이미지 저장 경로 설정
		String contextRoot = tempUploadPath;

		// 디렉토리가 없을경우 생성
		File directory = new File(tempUploadPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}

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

				// 요청의 절대 경로 -> localhost:10000
				String baseUrl =
					request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getRequestURI()))
						+ request.getContextPath();

				// 이미지의 URL 생성
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

	// 업로드된 이미지의 가로세로 정보 추출
	private List<Integer> calcImageLength(MultipartFile multipartFile) throws IOException {

		List<Integer> imagesLength = new ArrayList<>();

		try (InputStream inputStream = multipartFile.getInputStream()) {

			BufferedImage bufferedImage = ImageIO.read(inputStream);

			imagesLength.add(bufferedImage.getWidth());
			imagesLength.add(bufferedImage.getHeight());

			log.info("imagesLength: " + imagesLength);

		} catch (IOException e) {
			log.error("CalcImageLength Error!");
			throw e;
		}
		return imagesLength;
	}

	// summernote 취소버튼 누를 때 임시파일 제거
	@Override
	public void deleteSummernoteImage(List<String> uuids, List<String> extensions) {
		List<String> fileNames = new ArrayList<>();

		for (int i = 0; i < uuids.size(); i++) {
			fileNames.add(uuids.get(i) + extensions.get(i));
		}
			deleteTempFiles.deleteTempFile(fileNames);

	}

	@Override
	public void deleteSummernoteImageWithBackspace(List<String> deletedImageUrl) {

		for (String fileUrl : deletedImageUrl) {
			String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

			log.info("Delete File With BackSpace: {}", fileName);
			deleteTempFiles.deleteSingleTempFile(fileName);
		}
	}

	// summernote 로 임시파일 업로드 후 게시글 등록하면 자동 s3 업로드
	@Override
	public ResponseEntity<List<String>> uploadS3Image(List<MultipartFile> multipartFile, Post postNo,
		List<String> uuids) {

		List<String> error = new ArrayList<>();
		List<String> uploadResult = new ArrayList<>();

		if (multipartFile == null || multipartFile.isEmpty()) {

			log.error("업로드된 파일 없음!");

			error.add("업로드된 파일이 없음!");
			error.add(String.valueOf(postNo));
			error.add(String.valueOf(System.currentTimeMillis()));

			return ResponseEntity.badRequest().body(error);
		}

		log.info("Uuid size: {}개, multipartFileSize: {}개", uuids.size(), multipartFile.size());

		for (int i = 0; i < uuids.size(); i++) {

			MultipartFile file = multipartFile.get(i);

			String originalFileName = file.getOriginalFilename();
			String fileUuid = uuids.get(i);

			log.info("업로드 할 파일: {}, UUID: {}", originalFileName, fileUuid);

			try {
				// s3업로드 실행 전에 게시글 등록 로직 먼저 실행 후 반환 되는 postNo
				uploadResult = s3Uploader.upload(file, postNo, fileUuid);

			} catch (Exception e) {
				log.error("S3 업로드 에러", e);
				error.add("S3 업로드 에러: " + e.getMessage());
				error.add(String.valueOf(postNo));
				error.add(originalFileName);
				error.add(fileUuid);
				error.add(String.valueOf(System.currentTimeMillis()));
				return ResponseEntity.status(500).body(error);
			}
		}

		return ResponseEntity.ok(uploadResult);
	}

}