package com.community.dogcat.util.uploader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DeleteTempFiles {

	@Value("${tempUploadPath}")
	private String tempUploadPath;

	public void deleteFile(String fileName) {

		File file = new File(tempUploadPath, fileName);

		try {
			file.delete();
		} catch (Exception e) {
			log.error("썸머노트 임시파일 리스트 삭제 에러! - 이미 삭제 되었거나 존재하지 않습니다.");
		}
	}
}
