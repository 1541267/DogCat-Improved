package com.community.dogcat.dto.uploadImage;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
// 개선, 레디스 캐싱 | repository 사용 시 file의 uuid + 확장자를 위한 Dto
public class FileInfoDTO {
	private String uuid;
	private String extension;
	private String uploadTime;
	private String uploadPath;
	private String uploadThumbPath;
	private boolean deletePossible;

	public FileInfoDTO(String uuid, String extension, Instant uploadTime, boolean deletePossible) {

		LocalDate date = LocalDate.ofInstant(uploadTime, ZoneId.systemDefault());
		this.uuid = uuid;
		this.extension = extension;
		this.uploadTime = String.format("%04d/%02d/%02d",
			date.getYear(), date.getMonthValue(), date.getDayOfMonth());
		this.deletePossible = deletePossible;

		// 개선, 레디스 캐싱 or 조회를 위해
		this.uploadPath =
			"C:/testupload/uploaded/" + this.uploadTime + "/" + uuid.substring(0, 2) + "/" + getFullName();
		this.uploadThumbPath =
			"C:/testupload/uploaded/" + this.uploadTime + "/" + uuid.substring(0, 2) + "/t_" + getFullName();
	}

	public String getFullName() {
		return uuid + extension;
	}

}
