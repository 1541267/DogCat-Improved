package com.community.dogcat.util.uploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DeleteTempFiles {

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	@Value("${baseUrl}")
	private String baseUrl;

	public DeleteTempFiles(UploadRepository uploadRepository) {
		this.uploadRepository = uploadRepository;
	}

	private final UploadRepository uploadRepository;

	public void deleteFile(String fileName) {

		File file;
		Path thumbFile = Paths.get(tempUploadPath + "/thumbnail/", fileName);

		if (fileName.charAt(0) == 't' && fileName.charAt(1) == '_') {
			file = new File(tempUploadPath + "/thumbnail/", fileName);
		} else {
			file = new File(tempUploadPath, fileName);
		}

		try {
			if (thumbFile.toFile().exists()) {
				thumbFile.toFile().delete();
				uploadRepository.delete(ImgBoard.builder().fileUuid(fileName).build());
			} else {
				file.delete();
			}
		} catch (Exception e) {
			log.error("썸머노트 임시파일 리스트 삭제 에러! - 이미 삭제 되었거나 존재하지 않습니다.");
		}
	}
}
