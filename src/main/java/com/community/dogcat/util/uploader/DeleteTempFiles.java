package com.community.dogcat.util.uploader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeleteTempFiles {

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	// s3 업로드시 썸머노트 임시 파일 삭제 + 썸머노트 게시글 취소시 파일 삭제
	public void deleteTempFile(List<String> fileNames) {

		for (String fileName : fileNames) {
			deleteFile(fileName);
		}
	}

	// s3 업로드시 썸머노트 임시 파일 삭제 + 썸머노트 게시글 취소시 파일 삭제
	public void deleteSingleTempFile(String fileName) {
		deleteFile(fileName);
	}

	private void deleteFile(String fileName) {

		File file = new File(tempUploadPath, fileName);

		try {
			file.delete();
		} catch (Exception e) {
			log.error("썸머노트 임시파일 리스트 삭제 에러! - 이미 삭제 되었거나 존재하지 않습니다.");
		}
	}
}
