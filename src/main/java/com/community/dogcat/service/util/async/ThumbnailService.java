package com.community.dogcat.service.util.async;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ThumbnailService {

	private final ThreadPoolTaskExecutor ioExecutor;
	private final ThreadPoolTaskExecutor cpuExecutor;

	// ImageWriter 캐시
	private final ConcurrentMap<String, ImageWriter> writerCache = new ConcurrentHashMap<>();

	public ThumbnailService(
		@Qualifier("ioExecutor") ThreadPoolTaskExecutor ioExecutor,
		@Qualifier("cpuExecutor") ThreadPoolTaskExecutor cpuExecutor
	) {
		this.ioExecutor = ioExecutor;
		this.cpuExecutor = cpuExecutor;
	}

	/**
	 * 썸네일 생성만 별도 CPU Bouind 풀에서 실행
	 * baseUploadPath = 저장 경로 + 날짜
	 */
	// @Async("ioExecutor"), kafka로 위임, 비동기 제거
	// I/O ↔ CPU 스레드풀 분리:
	// 파일 읽기/쓰기: ioExecutor
	// 이미지 리사이징: cpuExecutor
	// ImageWriter 재사용 풀: 포맷별 ImageWriter를 캐시하여 매번 탐색 오버헤드를 제거
	// 비동기 예외 처리: .exceptionally() 로 로깅 및 알림 처리
	// 최종 체인 조합: 읽기 → 리사이징 → 쓰기 단계를 명확히 분리
	public CompletableFuture<Void> createThumbnails(List<FileInfoDTO> infos, String baseUploadPath
	) {

		int TARGET_WIDTH = 200;
		float JPEG_QUALITY = 0.85f;

		List<CompletableFuture<Void>> tasks = infos.stream()
			.map(info -> {
				Path srcPath = Path.of(info.getUploadPath());
				String format = getFormatName(info.getFullName());

				// Stage 1: 원본 읽기 (I/O)
				CompletableFuture<BufferedImage> readStage = CompletableFuture.supplyAsync(() -> {
					try (InputStream is = new BufferedInputStream(Files.newInputStream(srcPath))) {
						return ImageIO.read(is);
					} catch (IOException e) {
						throw new UncheckedIOException("원본 읽기 실패: " + srcPath, e);
					}
				}, ioExecutor);

				// Stage 2: 리사이징 (CPU)
				CompletableFuture<BufferedImage> resizeStage = readStage.thenApplyAsync(srcImg -> {
					BufferedImage fast = Scalr.resize(
						srcImg,
						Scalr.Method.SPEED,
						Scalr.Mode.FIT_TO_WIDTH,
						TARGET_WIDTH * 2
					);
					return Scalr.resize(
						fast,
						Scalr.Method.BALANCED,
						Scalr.Mode.FIT_TO_WIDTH,
						TARGET_WIDTH
					);
				}, cpuExecutor);

				// Stage 3: 썸네일 쓰기 (I/O)
				return resizeStage.thenAcceptAsync(thumbnail -> {
						Path thumbDir = srcPath.getParent().resolve("thumbnail");
						try {
							Files.createDirectories(thumbDir);
							Path dest = thumbDir.resolve("t_" + info.getFullName());

							// 매 작업마다 새로운 ImageWriter 생성
							ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
							ImageWriteParam param = writer.getDefaultWriteParam();
							if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
								param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
								param.setCompressionQuality(JPEG_QUALITY);
							}

							try (
								OutputStream os = new BufferedOutputStream(Files.newOutputStream(dest));
								ImageOutputStream ios = ImageIO.createImageOutputStream(os)
							) {
								writer.setOutput(ios);
								writer.write(null, new IIOImage(thumbnail, null, null), param);
							} finally {
								writer.dispose();
							}
						} catch (IOException e) {
							throw new UncheckedIOException("썸네일 쓰기 실패: " + info, e);
						}
					}, ioExecutor)
					.exceptionally(ex -> {
						log.error("썸네일 생성 체인 중 예외: {}", info, ex);
						return null;
					});
			})
			.toList();

		return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
	}

	//
	// 	int TARGET_WIDTH = 200;
	// 	float JPEG_QUALITY = 0.85f;
	//
	// 	List<CompletableFuture<Void>> tasks = infos.stream()
	// 		.map(info -> CompletableFuture.runAsync(() -> {
	// 			try {
	// 				String stored = computeStoredPath(info, baseUploadPath);
	// 				String thumbDir = stored.replace(info.getFullName(), "thumbnail/");
	// 				Files.createDirectories(Path.of(thumbDir));
	//
	// 				// 2) 버퍼링된 스트림으로 원본 읽기
	// 				BufferedImage srcImg;
	// 				try (InputStream is =
	// 						 new BufferedInputStream(Files.newInputStream(Path.of(stored)))) {
	// 					srcImg = ImageIO.read(is);
	// 				}
	//
	// 				// 3) 두 단계 리사이징
	// 				BufferedImage intermediate = Scalr.resize(
	// 					srcImg,
	// 					Scalr.Method.SPEED,
	// 					Scalr.Mode.FIT_TO_WIDTH,
	// 					TARGET_WIDTH * 2  // 2배 빠르게 축소
	// 				);
	//
	// 				BufferedImage thumbnail = Scalr.resize(
	// 					intermediate,
	// 					Scalr.Method.BALANCED,
	// 					Scalr.Mode.FIT_TO_WIDTH,
	// 					TARGET_WIDTH     // 최종 너비
	// 				);
	//
	// 				// 4) 포맷 결정 & 압축 파라미터 설정 (JPEG 한정)
	// 				String format = getFormatName(info.getFullName());
	// 				ImageWriter writer = ImageIO.getImageWritersByFormatName(format)
	// 					.next();
	// 				ImageWriteParam param = writer.getDefaultWriteParam();
	// 				if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
	// 					param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	// 					param.setCompressionQuality(JPEG_QUALITY);
	// 				}
	//
	// 				// 5) 버퍼링된 스트림으로 썸네일 쓰기
	// 				Path destPath = Path.of(thumbDir, "t_" + info.getFullName());
	// 				try (OutputStream os =
	// 						 new BufferedOutputStream(Files.newOutputStream(destPath));
	// 					 ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
	// 					writer.setOutput(ios);
	// 					writer.write(null, new IIOImage(thumbnail, null, null), param);
	// 					writer.dispose();
	// 				}
	//
	// 			} catch (IOException e) {
	// 				throw new UncheckedIOException("썸네일 생성 실패: " + info, e);
	// 			}
	// 		}, cpuExecutor)).toList();
	//
	// 	return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
	// }

	// 섬네일레이터
	// public CompletableFuture<Void> createThumbnails(List<FileInfoDTO> infos, String baseUploadPath) {
	//
	// 	List<CompletableFuture<Void>> tasks = infos.stream()
	// 		.map(info -> CompletableFuture.runAsync(() -> {
	// 			try {
	// 				String stored = computeStoredPath(info, baseUploadPath);
	// 				String thumbDir = stored.replace(info.getFullName(), "thumbnail/");
	// 				Files.createDirectories(Path.of(thumbDir));
	// 				Thumbnails.of(stored)
	// 					.size(200, 200)
	// 					.toFile(thumbDir + "t_" + info.getFullName());
	// 			} catch (IOException e) {
	// 				throw new RuntimeException(e);
	// 			}
	// 		}, cpuExecutor)).toList();
	//
	// 	return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
	// }

	private String getFormatName(String filename) {
		int idx = filename.lastIndexOf('.');
		return (idx >= 0)
			? filename.substring(idx + 1).toLowerCase()
			: "jpg";
	}

	private String computeStoredPath(FileInfoDTO info, String baseUploadPath) {

		String prefix = info.getUuid().substring(0, 2);
		return baseUploadPath + "/" + prefix + "/" + info.getFullName();

	}

}
