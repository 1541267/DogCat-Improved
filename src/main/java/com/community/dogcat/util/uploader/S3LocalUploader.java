package com.community.dogcat.util.uploader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnailator;

import com.community.dogcat.dto.uploadImage.UploadPostImageResultDTO;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class S3LocalUploader {

	@Value("${s3UploadPath}")
	private String uploadPath;

	public UploadPostImageResultDTO uploadLocal(MultipartFile multipartFile, String extension, String uuid) {

		if (multipartFile == null || multipartFile.isEmpty()) {
			return null;
		}

		// summernote 의 임시파일 uuid 를 받아옴
		String fileName = multipartFile.getOriginalFilename();
		String saveFileName = uuid + extension;

		Path savePath = Paths.get(uploadPath, saveFileName);

		List<String> savePathList = new ArrayList<>();

		try {
			multipartFile.transferTo(savePath);
			savePathList.add(savePath.toFile().getAbsolutePath());

			// 업로드 파일이 이미지 일 때
			if (Files.probeContentType(savePath).startsWith("image")) {
				File thumbFile = new File(uploadPath, "t_" + saveFileName);

				String thumbnailPath = thumbFile.getAbsolutePath();
				log.info("---------------- thumbnailPath: " + thumbnailPath);
				savePathList.add(uploadPath + "t_" + saveFileName);
				Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200, 200);

				return buildResultDto(uuid, fileName, extension, Instant.now(), true,
					uploadPath, thumbnailPath);

			} else {
				// 이미지가 아닐 때
				return buildResultDto(uuid, fileName, extension, Instant.now(), false,
					uploadPath, null);
			}

		} catch (Exception e) {
			log.error("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
		log.info("--------------------UploadPostImage: {}", Collections.singletonList(uuid + saveFileName));

		return null;
	}

	// 썸네일 파일을 db에 저장하기 위해 File 객체를 MultipartFile 로 변환
	public MultipartFile convertFileToMultipartFile(File file) throws IOException {
		FileInputStream input = new FileInputStream(file);
		return new MockMultipartFile("file", file.getName(), "image/jpeg", input);
	}

	// 중복코드 제거를 위한 메서드
	private UploadPostImageResultDTO buildResultDto(String uuid, String fileName, String extension, Instant uploadTime,
		boolean isImg, String uploadPath, String thumbnailPath) {

		return UploadPostImageResultDTO.builder()
			.uuid(uuid)
			.fileName(fileName)
			.extension(extension)
			.uploadTime(uploadTime)
			.img(isImg)
			.uploadPath(uploadPath)
			.thumbnailPath(thumbnailPath)
			.build();
	}

}
