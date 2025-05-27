package com.community.dogcat.service.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
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

	private final UploadedImageCaching uploadedImageCaching;
	private final TempFileService tempFileService;
	private final StorageService storageService;
	private final ThumbnailService thumbnailService;

	// 1) 임시 저장
	public void handleTempSave(List<FileInfoDTO> dtos, List<MultipartFile> files) {
		tempFileService.saveTempFileAsync(dtos, files);
	}

	// 2) 최종 복사 + 캐시
	public void handleFinalSave(List<FileInfoDTO> dtos, String baseUploadPath) {
		storageService.processUploadedFiles(dtos, baseUploadPath);
	}

	// 3) 삭제된 파일 캐시 제거
	public void handleDeletedFiles(List<FileInfoDTO> deletedFiles) {
		storageService.processDeletedFiles(deletedFiles);
	}

	// 4) 썸네일 생성
	public void handleThumbnails(List<FileInfoDTO> dtos, String baseUploadPath) {
		thumbnailService.createThumbnails(dtos, baseUploadPath);
	}

}