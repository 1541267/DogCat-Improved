package com.community.dogcat.service.util.async;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;

@Service
public class TempFileService {

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	@Async("ioExecutor")
	public CompletableFuture<Void> saveTempFileAsync(List<FileInfoDTO> dtos, List<MultipartFile> multipartFile) {

		for (int i = 0; i < dtos.size(); i++) {
			FileInfoDTO dto = dtos.get(i);
			MultipartFile file = multipartFile.get(i);
			Path dest = Paths.get(tempUploadPath + dto.getFullName());

			try {
				Files.createDirectories(dest.getParent());

				try (InputStream in = file.getInputStream()) {
					Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException e) {
				throw new UncheckedIOException("임시 파일 저장 실패: " + dest, e);
			}
		}
		return CompletableFuture.completedFuture(null);
	}

}
