package com.community.dogcat.dto.uploadImage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.community.dogcat.domain.ImgBoard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Component
@NoArgsConstructor
@AllArgsConstructor
public class FindUploadImageDTO {

	@Autowired
	private static UploadPostImageResultDTO uploadPostImageResultDTO;

	private String fileUuid;
	private String fileName;
	private String uploadTime;
	private Long postNo;
	private String getLink;

	//ImgBoard 를 dto 로 변환
	public static FindUploadImageDTO from(ImgBoard imgBoard) {
		return FindUploadImageDTO.builder()
			.fileUuid(imgBoard.getFileUuid())
			.fileName(imgBoard.getFileName())
			.uploadTime(String.valueOf(imgBoard.getUploadTime()))
			.postNo(imgBoard.getPostNo().getPostNo()) // 첫 getPostNo는 ImgBoard, 두번째는 Post -> FK때문?
			// .getLink(uploadPostImageResultDTO.getLink()) // getLink 우선 주석처리
			.build();
	}
}
