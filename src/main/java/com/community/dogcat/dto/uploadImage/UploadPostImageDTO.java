package com.community.dogcat.dto.uploadImage;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class UploadPostImageDTO {

	private List<MultipartFile> files;
	private Long postNo;
}
