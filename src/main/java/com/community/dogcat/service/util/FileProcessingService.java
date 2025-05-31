package com.community.dogcat.service.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.dto.uploadImage.ThumbnailRequestPayload;
import com.community.dogcat.service.util.async.StorageService;
import com.community.dogcat.service.util.async.TempFileService;
import com.community.dogcat.service.util.async.ThumbnailService;
import com.community.dogcat.util.cache.UploadedImageCaching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileProcessingService {

	private final TempFileService tempFileService;
	private final StorageService storageService;

	// 썸네일 진행중인 파일 체크를 위한 원본 파일 캐싱
	private final RedisTemplate<String, String> rt;
	private final ThreadPoolTaskExecutor ioExecutor;
	@Qualifier("cpuExecutor") private final ThreadPoolTaskExecutor cpuExecutor;
	private final KafkaTemplate<String, ThumbnailRequestPayload> kafkaTemplate;

	// 1) 임시 저장
	public CompletableFuture<Void> handleTempSave(List<FileInfoDTO> dtos, List<MultipartFile> files) {
		return tempFileService.saveTempFileAsync(dtos, files);
	}

	// 2) 최종 복사 + 캐시
	@Async("ioExecutor")
	public CompletableFuture<Void> handleFinalSave(List<FileInfoDTO> dtos, String baseUploadPath) {
		String PROCESSING_KEY = "imgboard:thumbnail:processing";

		String[] files = dtos.stream().map(FileInfoDTO::getFullName).toArray(String[]::new);

		// 썸네일 처리 중 표시, consummer (kafkaListener) 에서 끝나면 삭제
		rt.opsForSet().add(PROCESSING_KEY, files);
		// 2) 실제 파일 복사(→디스크 또는 S3 업로드) : 반드시 ioExecutor에서 실행

		CompletableFuture<Void> copyStage = CompletableFuture.runAsync(() -> {
			// storageService.processFinalFiles 내부에서
			// → Files.copy 또는 S3 업로드 등의 블로킹 I/O가 일어남
			storageService.processUploadedFiles(dtos, baseUploadPath);
		}, ioExecutor);

		// 3) “copyStage”가 완료된 뒤에만 Kafka로 썸네일 요청 데이터 전송
		CompletableFuture<Void> kafkaStage = copyStage.thenRunAsync(() -> {
			ThumbnailRequestPayload payload = new ThumbnailRequestPayload(dtos, baseUploadPath);
			kafkaTemplate.send("thumbnail-Generator", payload);
		}, ioExecutor);
		return kafkaStage;
		// return storageService.processUploadedFiles(dtos, baseUploadPath)
		// 	.thenRun(() ->
		// 		kafkaTemplate.send("thumbnail-Generator", new ThumbnailRequestPayload(dtos, baseUploadPath)));
	}

	// 3) 삭제된 파일 캐시 제거
	public void handleDeletedFiles(List<FileInfoDTO> deletedFiles) {
		storageService.processDeletedFiles(deletedFiles);
	}

	// 4) 썸네일 생성, kafka로 전환
	// public void handleThumbnails(List<FileInfoDTO> dtos, String baseUploadPath) {
	// 	thumbnailService.createThumbnails(dtos, baseUploadPath);
	// }

}