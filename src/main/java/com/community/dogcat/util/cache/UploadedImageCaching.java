package com.community.dogcat.util.cache;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * 애플리케이션 실행 시 전체 이미지 정보 캐싱
 * */
@SuppressWarnings("ConstantConditions")
public class UploadedImageCaching {

	private final RedisTemplate<String, String> rt;

	/** 게시글 등록 | 수정 시 | 삭제 시 캐싱을 파이프라이닝 기존엔 업로드 파일 하나당 put 으로 네트워크의 부하 증가 -> 하나의 파이프라인으로 모아서 한번에 요청 
	 * args 1 = 업로드된 파일들, args 2 = 삭제된 파일들 */
	public void cacheMetadataAddOrDelete(List<FileInfoDTO> uploadedFiles, List<FileInfoDTO> deletedFiles) {
		rt.executePipelined((RedisCallback<Object>)conn -> {

			byte[] metaHashKey = rt.getStringSerializer().serialize("imgboard:meta");
			byte[] toDeleteHashKey = rt.getStringSerializer().serialize("imgboard:toDelete");

			if (!uploadedFiles.isEmpty()) {
				for (FileInfoDTO dto : uploadedFiles) {
					conn.hSet(metaHashKey,
						rt.getStringSerializer().serialize(dto.getFullName()),
						rt.getStringSerializer().serialize(dto.getUploadPath() + "|" + dto.getUploadThumbPath()));
				}
			}

			if (!deletedFiles.isEmpty()) {
				for (FileInfoDTO dto : deletedFiles) {
					byte[] field = rt.getStringSerializer().serialize(dto.getFullName());
					byte[] value = rt.getStringSerializer()
						.serialize(dto.getUploadPath() + "|" + dto.getUploadThumbPath());
					conn.hDel(metaHashKey, field);
					conn.hSet(toDeleteHashKey, field, value);
				}
			}
			return null;
		});
	}

}