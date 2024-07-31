package com.community.dogcat.util;

import static org.apache.commons.io.file.PathUtils.*;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCheckTask {

	@Value("${uploadPath}")
	private String uploadPath;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	private final AmazonS3 s3Client;

	private final UploadRepository uploadRepository;

	//TODO 꼭 배포 전에 활성화 시키기
	// 매월 1일 자정에 요일무시 파일 정리 실행
	// @Scheduled(cron = "0 0 0 1 * ?")

	@Transactional
	public void checkFiles() throws Exception {
		log.info("===========================================");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 EEEE");
		log.info("File Check Task run.................");
		log.info("오늘은 {} ", LocalDateTime.now().format(formatter) + " 입니다.");
		log.info("-------------------------------------------");

		// 남아있는 모든 임시 데이터 모두 삭제
		if (Paths.get(uploadPath).toFile().exists()) {
			deleteDirectory(Paths.get(uploadPath));
			log.info("Upload Directory: 삭제 완료.");
		} else {
			log.info("Upload Directory: 남아있는 파일이 없습니다.");
		}
		log.info("-------------------------------------------");
		// S3 버킷과 DB의 이미지 테이블과 비교해 S3에 없는 파일 제거
		List<String> uploadedFiles = fileListInBucket();
		deleteUploadedFiles(uploadedFiles);

	}

	// 버킷에 업로드된 파일들의 키값(파일이름)을 가져옴
	private List<String> fileListInBucket() {

		List<S3ObjectSummary> uploadedS3Files;

		// 버킷에 업로드된 모든 오브젝트를 리스트로 받아옴
		uploadedS3Files = s3Client.listObjects(bucketName).getObjectSummaries();

		List<String> uploadedFiles = new ArrayList<>();

		// 썸네일 파일 제외하고 리스트로 넣음
		for (S3ObjectSummary uploadedS3File : uploadedS3Files) {

			if (!uploadedS3File.getKey().contains("t_")) {

				String fileName = uploadedS3File.getKey();

				uploadedFiles.add(fileName);
			}
		}
		return uploadedFiles;
	}

	// ImgBoard 에서 파일 찾기 위한 Uuid 추출
	private String extractionUuid(String originalFileName) {

		int lastIndex = originalFileName.lastIndexOf(".");

		return lastIndex != -1 ? originalFileName.substring(0, lastIndex) : originalFileName;

	}

	// DB에 저장되어있지 않는 업로드 파일들 제거
	private void deleteUploadedFiles(List<String> uploadedFiles) {

		if (!uploadedFiles.isEmpty()) {
			log.info("정리 할 S3 업로드 이미지가 없습니다");
		}

		for (String uploadedFile : uploadedFiles) {
			String fileUuid = extractionUuid(uploadedFile);

			Optional<ImgBoard> findImageInDatabase = uploadRepository.findById(fileUuid);

			if (findImageInDatabase.isEmpty()) {

				String thumbUploadedFile = "t_" + uploadedFile;

				s3Client.deleteObject(bucketName, uploadedFile);
				s3Client.deleteObject(bucketName, thumbUploadedFile);
			}
		}

		log.info("-------------------------------------------");
		log.info("DB에 존재하지 않는 S3업로드 이미지 정리 완료");
		log.info("===========================================");
	}
}