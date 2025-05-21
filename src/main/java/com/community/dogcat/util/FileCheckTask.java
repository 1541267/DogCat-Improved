package com.community.dogcat.util;

import static org.apache.commons.io.file.PathUtils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor

/**
 * 정기적인 캐시 & 디스크 파일 정리 & 매일 자정 5분 전에 전체적인 정리
 * */
public class FileCheckTask {

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	@Value("${tempUploadPath}thumbnail")
	private String tempUploadThumbPath;

	@Value("${finalUploadPath}")
	private String finalUploadPath;

	@Autowired
	private RedisTemplate<String, String> rt;

	private final UploadRepository uploadRepository;

	private final String bigLogLine = "===========================================";
	private final String smolLogLine = "-------------------------------------------";

	private final String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

	// s3 비활성
	// @Value("${cloud.aws.s3.bucket}")
	// private String bucketName;
	// private final AmazonS3 s3Client;

	/**
	 * 매월 1일 자정에 요일무시 파일 정리 실행
	 * @Scheduled(cron = "0 0 0 1 * ?")
	 * 매일 새벽 5시
	 * @Scheduled(cron = "0 0 5 * * ?", zone = "Asia/Seoul")
	 * 실행 시 서버 점검으로 설정
	 * 매일 자정
	 * @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
	 * 매일 자정 5분 전
	 * @Scheduled(cron = "0 55 23 * * *")
	 * 매월 마지막 날 자정 5분 전
	 */

	@Scheduled(cron = "0 55 23 * * *", zone = "Asia/Seoul")
	@Transactional
	public void checkFiles() throws Exception {
		log.info(bigLogLine);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 EEEE");
		log.info("File Check Task run.................");
		log.info("오늘은 {} ", LocalDateTime.now().format(formatter) + " 입니다.");
		log.info(smolLogLine);

		Path tempPath = Paths.get(tempUploadPath);
		Path tempThumbPath = Paths.get(tempUploadThumbPath);
		// 남아있는 모든 임시 데이터 모두 삭제 후 폴더 생성

		if (Files.exists(tempPath) || Files.exists(tempThumbPath)) {
			deleteDirectory(tempPath);
			Files.createDirectories(tempThumbPath);
			log.info("Monthly Temp File Check: 삭제 완료.");
		} else {
			log.info("Monthly Temp File Check: 남아있는 파일이 없습니다.");
		}
		log.info(smolLogLine);
		// 개선, db와 디스크 비교 후 db에는 없는 이미지 삭제 
		// + 삭제 마킹된 이미지 db 제거
		cleanUpOrphanFiles();

		// 매월 마지막 날 이면 전체 전체 디렉토리 검사, 비어있는 폴더 삭제
		if (LocalDate.now().getDayOfMonth() == LocalDate.now().lengthOfMonth()) {
			directoryCleaner();
		}

		log.info("Monthly File Check : 완료");
		// S3 버킷과 DB의 이미지 테이블과 비교해 S3에 없는 파일 제거
		// List<String> uploadedFiles = fileListInBucket();
		// deleteUploadedFiles(uploadedFiles);

	}

	// db에 저장 되어있지 않고 남아있는 파일 제거
	private void cleanUpOrphanFiles() throws IOException {

		Path dir = Paths.get(finalUploadPath);

		// 수정으로 인한 삭제 가능 파일들
		Set<FileInfoDTO> orphanFiles = uploadRepository.findFileUuidAndExtensionAndUploadTimeAndDeletePossibleByDeletePossibleTrue();
		Set<Object> toDelete = rt.opsForHash().keys("imgboard:toDelete");



		// 임시, 나중에 지울 것
		Set<FileInfoDTO> temp = uploadRepository.findFileUuidAndExtensionAndUploadTimeAndDeletePossibleByDeletePossibleFalse();

		// db에 deletePossible이 true인 파일 삭제 후 마지막에 db에서 제거, 날짜 상관 없음
		// if (!orphanFiles.isEmpty()) {
		// 	for (FileInfoDTO dto : orphanFiles) {
		//
		// 		String prefix = dto.getUuid().substring(0, 2);
		// 		String uploadTime = dto.getUploadTime();
		// 		Path file = dir.resolve(uploadTime).resolve(prefix).resolve(dto.getFullName());
		//
		// 		Path thumbFile = dir.resolve(uploadTime)
		// 			.resolve(prefix)
		// 			.resolve("thumbnail")
		// 			.resolve("t_" + dto.getFullName());
		//
		// 		Files.deleteIfExists(file);
		// 		Files.deleteIfExists(thumbFile);
		//
		// 		log.info("file: {}, thumbFile: {}", file, thumbFile);
		// 	}
		// }
		// uploadRepository.deleteAllByDeletePossibleTrue();

		// 정기적인 캐시 정리에도 남아있는 캐시와 파일 정리를 위해
		Set<Object> keys = rt.opsForHash().keys("imgboard:meta");

		// if (!keys.isEmpty()) {
		// 	for (Object key : keys) {
		// 		String value = (String)rt.opsForHash().get("imgboard:meta", key);
		// 		String[] a = value.split("\\|");
		// 		System.out.println("==========================================================");
		// 		System.out.println("Value 0 : " + a[0] + "\nValue 1 : " + a[1]);
		// 	}
		// }

		Set<String> dbFileNames = uploadRepository.findAllFullName();
		Set<String> allThumbFileNames = dbFileNames.stream()
			.map(names -> "t_" + names)
			.collect(Collectors.toSet());

		Path baseDir = Path.of(finalUploadPath + today);
		System.out.println("baseDir = " + baseDir);

		// 1. 일반 파일 검사 및 해당 썸네일 같이 삭제
		try (Stream<Path> paths = Files.walk(baseDir)) {
			paths.filter(Files::isRegularFile).forEach(file -> {
				Path fileName = file.getFileName();
				Path parent = file.getParent();

				// thumbnail 폴더는 이 단계에서 무시 (2단계에서 별도로 처리)
				if (parent.getFileName().toString().equalsIgnoreCase("thumbnail"))
					return;

				String baseName = fileName.toString();

				if (!dbFileNames.contains(baseName)) {
					try {
						// 원본 삭제
						Files.delete(file);
						System.out.println("삭제됨 (원본): " + file);

						// 썸네일 경로 구성 및 삭제 시도
						Path thumbPath = parent.resolve("thumbnail").resolve("t_" + baseName);
						if (Files.exists(thumbPath)) {
							Files.delete(thumbPath);
							System.out.println("삭제됨 (썸네일): " + thumbPath);
						}
					} catch (IOException e) {
						System.err.println("삭제 실패: " + file + " → " + e.getMessage());
					}
				}
			});
		}

		// 일단 썸네일만 남아 있다는 상황 없는 전재하에 원본만 정리
		// // 2. thumbnail 폴더만 따로 검사해서 잔여 썸네일 삭제
		// try (Stream<Path> paths = Files.walk(BASE_DIR)) {
		// 	paths.filter(Files::isRegularFile).forEach(file -> {
		// 		Path parent = file.getParent();
		// 		if (!parent.getFileName().toString().equalsIgnoreCase("thumbnail"))
		// 			return;
		//
		// 		String fileName = file.getFileName().toString();
		//
		// 		if (!thumbNames.contains(fileName)) {
		// 			try {
		// 				Files.delete(file);
		// 				System.out.println("삭제됨 (고아 썸네일): " + file);
		// 			} catch (IOException e) {
		// 				System.err.println("썸네일 삭제 실패: " + file + " → " + e.getMessage());
		// 			}
		// 		}
		// 	});
		// }

	}

	private void directoryCleaner() {

		log.info("마지막 날 디렉토리 클리너 완료");
	}

	// DB에 저장되어있지 않는 업로드 파일들 제거
	// private void deleteUploadedFiles(List<String> uploadedFiles) {
	//
	// 	if (uploadedFiles.isEmpty()) {
	// 		log.info("정리 할 업로드된 이미지가 없습니다");
	// 		// log.info("정리 할 S3 업로드 이미지가 없습니다");
	// 		return;
	// 	}
	//
	// 	// for (String uploadedFile : uploadedFiles) {
	// 	// 	String fileUuid = extractionUuid(uploadedFile);
	// 	//
	// 	// 	Optional<ImgBoard> findImageInDatabase = uploadRepository.findById(fileUuid);
	// 	//
	// 	// 	if (findImageInDatabase.isEmpty()) {
	// 	//
	// 	// 		String thumbUploadedFile = "t_" + uploadedFile;
	// 	//
	// 	// 		// s3 -> local로 전환
	// 	// 		// s3Client.deleteObject(bucketName, uploadedFile);
	// 	// 		// s3Client.deleteObject(bucketName, thumbUploadedFile);
	// 	//
	// 	// 	}
	// 	// }
	//
	// 	log.info(smolLogLine);
	// 	// log.info("DB에 존재하지 않는 S3업로드 이미지 정리 완료");
	// 	log.info("DB에 존재하지 않는 임시 | 업로드된 이미지 정리 완료");
	// 	log.info(bigLogLine);
	// }

	// ImgBoard 에서 파일 찾기 위한 Uuid 추출
	// private String extractionUuid(String originalFileName) {
	//
	// 	int lastIndex = originalFileName.lastIndexOf(".");
	//
	// 	return lastIndex != -1 ? originalFileName.substring(0, lastIndex) : originalFileName;
	//
	// }
	// s3 비활성, 버킷에 업로드된 파일들의 키값(파일이름)을 가져옴
	// private List<String> fileListInBucket() {
	// 	List<S3ObjectSummary> uploadedS3Files;
	//
	// 	// 버킷에 업로드된 모든 오브젝트를 리스트로 받아옴
	// 	uploadedS3Files = s3Client.listObjects(bucketName).getObjectSummaries();
	//
	// 	List<String> uploadedFiles = new ArrayList<>();
	//
	// 	// 썸네일 파일 제외하고 리스트로 넣음
	// 	for (S3ObjectSummary uploadedS3File : uploadedS3Files) {
	//
	// 		if (!uploadedS3File.getKey().contains("t_")) {
	//
	// 			String fileName = uploadedS3File.getKey();
	//
	// 			uploadedFiles.add(fileName);
	// 		}
	// 	}
	// 	return uploadedFiles;
	// }

}