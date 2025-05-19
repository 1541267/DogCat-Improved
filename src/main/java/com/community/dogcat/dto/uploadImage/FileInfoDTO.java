package com.community.dogcat.dto.uploadImage;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
// 개선, repository 사용 시 file의 uuid + 확장자를 위한 Dto
public class FileInfoDTO {
	private String uuid;
	private String extension;

	public FileInfoDTO(String uuid, String extension) {
		this.uuid = uuid;
		this.extension = extension;
	}

	public String getFullName() {
		return uuid + extension;
	}

}
