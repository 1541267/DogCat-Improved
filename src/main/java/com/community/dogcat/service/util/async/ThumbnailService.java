package com.community.dogcat.service.util.async;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import net.coobird.thumbnailator.Thumbnails;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;

@Service
public class ThumbnailService {

	private final ThreadPoolTaskExecutor cpuExecutor;

	public ThumbnailService(		@Qualifier("cpuExecutor") ThreadPoolTaskExecutor cpuExecutor) {this.cpuExecutor = cpuExecutor;}

	/**
	 * 썸네일 생성만 별도 CPU Bouind 풀에서 실행
	 * baseUploadPath = 저장 경로 + 날짜
	 */
	@Async("ioExecutor")
	public CompletableFuture<Void> createThumbnails(List<FileInfoDTO> infos, String baseUploadPath) {
		infos.forEach(info -> {
			try {
				String stored = computeStoredPath(info, baseUploadPath);
				String thumbDir = stored.replace(
					info.getFullName(), "thumbnail/");
				Files.createDirectories(Path.of(thumbDir));
				Thumbnails.of(stored)
					.size(200, 200)
					.toFile(thumbDir + "t_" + info.getFullName());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		return CompletableFuture.completedFuture(null);
	}

	private String computeStoredPath(FileInfoDTO info, String baseUploadPath) {

		String prefix = info.getUuid().substring(0, 2);
		return baseUploadPath + "/" + prefix + "/" + info.getFullName();

	}

}
