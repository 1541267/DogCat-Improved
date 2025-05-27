package com.community.dogcat.service.util.async;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.util.cache.UploadedImageCaching;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageService {

	private final UploadedImageCaching uploadedImageCaching;

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	/** 파일 업로드 시 파일 복사 & 이미지 캐시 업데이트
	 * ioExecutor 비동기 사용 */
	@Async("ioExecutor")
	public CompletableFuture<Void> processUploadedFiles(List<FileInfoDTO> uploadedFiles, String baseUploadPath) {

		uploadedFiles.forEach(info -> {
			try {
				copyToFinalLocation(info, baseUploadPath);
			} catch (IOException e) {
				throw new RuntimeException("파일 복사 실패: " + info.getFullName(), e);
			}
		});

		uploadedImageCaching.cacheMetadataAddOrDelete(uploadedFiles, new ArrayList<>());
		return CompletableFuture.completedFuture(null);
	}

	/** 게시글 수정 시 캐시 삭제 비동기를 위해 */
	@Async("ioExecutor")
	public CompletableFuture<Void> processDeletedFiles(List<FileInfoDTO> deletedFiles) {

		uploadedImageCaching.cacheMetadataAddOrDelete(new ArrayList<>(), deletedFiles);

		return CompletableFuture.completedFuture(null);
	}

	// 파일 복사 (제로 카피 FileChannel.transferTo)
	protected void copyToFinalLocation(FileInfoDTO info, String baseUploadPath) throws IOException {

		Path src = Path.of(tempUploadPath + info.getFullName());
		String prefix = info.getUuid().substring(0, 2);
		Path destDir = Path.of(baseUploadPath + prefix);
		Files.createDirectories(destDir);
		Path dest = destDir.resolve(info.getFullName());

		try (var in = java.nio.channels.FileChannel.open(src, StandardOpenOption.READ);
			 var out = java.nio.channels.FileChannel.open(
				 dest,
				 StandardOpenOption.CREATE,
				 StandardOpenOption.WRITE,
				 StandardOpenOption.TRUNCATE_EXISTING)) {
			long size = in.size();
			long pos = 0;
			while (pos < size) {
				pos += in.transferTo(pos, size - pos, out);
			}
		}
		Files.deleteIfExists(src);
	}

}
