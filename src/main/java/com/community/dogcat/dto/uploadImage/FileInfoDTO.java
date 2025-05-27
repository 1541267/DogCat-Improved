package com.community.dogcat.dto.uploadImage;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
// 개선, 레디스 캐싱 | repository 사용 시 file의 uuid + 확장자를 위한 Dto
public class FileInfoDTO {
	private String uuid;
	private String extension;
	private String uploadTime;
	private String uploadPath;
	private String uploadThumbPath;
	private String uploadUrl;
	private String uploadThumbUrl;
	private String prefix;
	private String originalName;
	private Post postNo;
	private boolean deletePossible;

	@Builder
	public FileInfoDTO(String uuid, String extension, Instant uploadTime, boolean deletePossible, String originalName) {

		LocalDate date = LocalDate.ofInstant(uploadTime, ZoneId.systemDefault());

		this.uuid = uuid;
		this.extension = extension;
		this.prefix = uuid.substring(0, 2);
		this.uploadTime = String.format("%04d/%02d/%02d",
			date.getYear(), date.getMonthValue(), date.getDayOfMonth());
		this.deletePossible = deletePossible;
		this.originalName = originalName;

		// 개선, 레디스 캐싱 or 조회를 위해
		this.uploadPath =
			"C:/testupload/uploaded/" + this.uploadTime + "/" + this.prefix + "/" + getFullName();
		this.uploadThumbPath =
			"C:/testupload/uploaded/" + this.uploadTime + "/" + this.prefix + "/thumbnail/t_" + getFullName();

		String finalUploadedUrl = "http://localhost:10000/uploaded/" + this.uploadTime;
		this.uploadUrl = finalUploadedUrl + "/" + this.prefix + getFullName();
		this.uploadThumbUrl = finalUploadedUrl + "/" + this.prefix + "/thumbnail/t_" + getFullName();
	}

	public String getFullName() {
		return uuid + extension;
	}

	public ImgBoard toEntity(Post postNo) {

		return ImgBoard.builder()
			.fileUuid(this.uuid)
			.extension(this.extension)
			.uploadTime(Instant.now())
			.uploadPath(this.uploadUrl)
			.thumbnailPath(this.uploadThumbUrl)
			.fileName(this.originalName)
			.postNo(postNo)
			.build();
	}

}
