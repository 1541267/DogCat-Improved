package com.community.dogcat.service.upload;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.Post;
import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadMetaService {

	private final UploadRepository uploadRepository;

	/**
	 * 파일 업로드 시 db 저장만 담당 (트랜잭션 축소를 위해)
	 */
	@Transactional
	public void saveImageToDB(List<FileInfoDTO> infos, Post postNo) {
		uploadRepository.saveAll(
			infos.stream().map(info -> info.toEntity(postNo)).toList()
		);
	}

}
