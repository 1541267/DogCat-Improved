package com.community.dogcat.util;

import static org.apache.commons.io.file.PathUtils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;
import com.community.dogcat.util.uploader.DeleteTempFiles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import scala.compiletime.ops.string;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCheckTask {

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	@Value("${tempUploadPath}thumbnail")
	private String tempUploadThumbPath;

	@Value("${finalUploadPath}")
	private String finalUploadPath;
	@Value("${finalUploadPath}thumbnail")
	private String finalUploadThumbPath;

	private final DeleteTempFiles deleteTempFiles;

	private final UploadRepository uploadRepository;

	private final String bigLogLine = "===========================================";
	private final String smolLogLine = "-------------------------------------------";

	// s3 비활성
	// @Value("${cloud.aws.s3.bucket}")
	// private String bucketName;
	// private final AmazonS3 s3Client;

	/** TODO 꼭 배포 전에 활성화 시키기
	 * 매월 1일 자정에 요일무시 파일 정리 실행
	 * @Scheduled(cron = "0 0 0 1 * ?")
	 * 매일 새벽 5시
	 * @Scheduled(cron = "0 0 5 * * ?", zone = "Asia/Seoul")
	 * 실행 시 서버 점검으로 표시
	 */
	@Scheduled(cron = "0 0 5 * * ?", zone = "Asia/Seoul")
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

		log.info("Monthly File Check : 완료");
		// S3 버킷과 DB의 이미지 테이블과 비교해 S3에 없는 파일 제거
		// List<String> uploadedFiles = fileListInBucket();
		// deleteUploadedFiles(uploadedFiles);

	}

	// db에 저장 되어있지 않고 남아있는 파일 제거
	private void cleanUpOrphanFiles() throws IOException {

		Path dir = Paths.get(finalUploadPath);
		Path thumbDir = Paths.get(finalUploadThumbPath);

		Set<FileInfoDTO> orphanFiles = uploadRepository.findFileUuidAndExtensionByDeletePossibleTrue();

		// db에 deletePossible이 true인 파일 삭제 후 마지막에 db에서 제거
		if (!orphanFiles.isEmpty()) {
			for (FileInfoDTO dto : orphanFiles) {
				Path file = dir.resolve(dto.getFullName());
				Path thumbFile = thumbDir.resolve(dto.getFullName());

				Files.deleteIfExists(file);
				Files.deleteIfExists(thumbFile);

				log.info("file: {}, thumbFile: {}", file, thumbFile);
			}
		}

		Set<String> allFileNames = uploadRepository.findAllFullName();
		Set<String> allThumbFileNames = allFileNames.stream()
			.map(names -> "t_" + names)
			.collect(Collectors.toSet());

		try (Stream<Path> files = Files.list(dir); Stream<Path> thumbs = Files.list(thumbDir)) {
			files.forEach(path -> {
				String fileName = path.getFileName().toString();
				System.out.println("fileName = " + fileName);
				if (!allFileNames.contains(fileName)) {
					try {
						Files.deleteIfExists(path);
						log.info("Deleted orphan file: {}", fileName);
					} catch (IOException e) {
						log.error("Failed to delete orphan file {}", fileName, e);
					}
				}
			});

			thumbs.forEach(path -> {
				String fileName = path.getFileName().toString();
				System.out.println("ThumbFileName = " + fileName);
				if (!allThumbFileNames.contains(fileName)) {
					try {
						Files.deleteIfExists(thumbDir.resolve(fileName));
						log.info("Deleted orphan file: {}", fileName);
					} catch (IOException e) {
						log.error("Failed to delete orphan file {}", fileName, e);
					}
				}
			});
		}

		uploadRepository.deleteAllByDeletePossibleTrue();

		// List<String> uuids = Files.list(dir)
		// 	.filter(Files::isRegularFile)
		// 	.map(p -> p.getFileName().toString())
		// 	.toList();
		//
		// List<String> thumbs = Files.list(thumbDir)
		// 	.filter(Files::isRegularFile)
		// 	.map(p -> p.getFileName().toString())
		// 	.toList();
		//
		// if (uuids.isEmpty() && thumbs.isEmpty()) {
		// 	log.info("Monthly Uploaded File Check : 삭제 할 파일이 없습니다");
		// 	return;
		// }
		//
		// Set<String> existingSet = uploadRepository.findExistingImages(uuids);
		// Set<String> existingThumbSet = uploadRepository.findExistingImages(thumbs);
		//
		// // 3) 메모리에서 비교 → 삭제 대상 UUID 목록
		// List<String> toDelete = uuids.stream()
		// 	.filter(id -> !existingSet.contains(id.split("\\.")[0]))
		// 	.toList();
		//
		// List<String> toDeleteTemp = thumbs.stream()
		// 	.filter(id -> !existingThumbSet.contains(id.split("\\.")[0]))
		// 	.toList();
		//
		// if (toDelete.isEmpty() && toDeleteTemp.isEmpty()) {
		// 	log.info("Monthly Uploaded File Check : 삭제 할 파일이 없습니다");
		// 	return;
		// }
		//
		// // 4) 실제 파일·썸네일 삭제
		// if (!toDelete.isEmpty()) {
		// 	for (String id : toDelete) {
		// 		Path file = dir.resolve(id);
		// 		Path thumb = dir.resolve("thumbnail").resolve("t_" + id);
		//
		// 		Files.deleteIfExists(file);
		// 		Files.deleteIfExists(thumb);
		// 		log.info("Deleted orphan files: {}, {}", file, thumb);
		// 	}
		// } else if (!toDeleteTemp.isEmpty()) {
		// 	for (String id : toDeleteTemp) {
		// 		Path thumb = thumbDir.resolve("thumbnail").resolve("t_" + id);
		//
		// 		Files.deleteIfExists(thumb);
		// 		log.info("Deleted orphan files: {}", thumb);
		// 	}
		// }

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