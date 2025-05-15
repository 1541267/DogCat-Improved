package com.community.dogcat.util.uploader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

	@Value("${finalUploadPath}")
	private String finalUploadPath;

	private UploadRepository uploadRepository;

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

	public void deleteFileAfterUpload(List<String> files) {

		for (String fileName : files) {
			File file = new File(tempUploadPath, fileName);
			file.delete();
		}
	}

	public void deleteUploadedFiles(List<ImgBoard> files) {

		for (ImgBoard imgBoard : files) {
			File thumb = new File(finalUploadPath + "thumbnail/",
				"t_" + imgBoard.getFileUuid() + imgBoard.getExtension());
			File file = new File(finalUploadPath, imgBoard.getFileUuid() + imgBoard.getExtension());
			log.info("업로드된 파일 삭제 (uuid={}, {})", file.getName(), thumb.getName());
			thumb.delete();
			file.delete();

		}

	}
}
