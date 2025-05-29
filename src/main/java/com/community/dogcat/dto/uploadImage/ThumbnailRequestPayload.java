package com.community.dogcat.dto.uploadImage;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
/** kafka 썸네일 처리용 */
public class ThumbnailRequestPayload {
	private List<FileInfoDTO> infos;
	private String baseDir;
}
