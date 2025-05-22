package com.community.dogcat.util.cache;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
/**
 * 애플리케이션 실행 시 전체 이미지 정보 캐싱
 * */
public class UploadedImageCaching {

	private final UploadRepository uploadRepository;
	private final RedisTemplate<String, String> rt;
	private final HashOperations<String, String, String> hashOps;

	public UploadedImageCaching(UploadRepository uploadRepository, RedisTemplate<String, String> redisTemplate) {
		this.uploadRepository = uploadRepository;
		this.rt = redisTemplate;
		this.hashOps = redisTemplate.opsForHash();
	}

	/** 애플리케이션 시작 직후 한 번 실행,
	 * 애플리케이션 시작 시 모든 이미지 메타데이터를 레디스에 캐싱 */
	@PostConstruct
	public void preloadCache() {

		long startTime = System.currentTimeMillis();

		log.info("==========================================================");
		log.info("Redis Caching...");

		Set<FileInfoDTO> files = uploadRepository.findAllFileUuidAndExtensionAndUploadTimeAndDeletePossible();

		files.forEach(file -> {
			if (!file.isDeletePossible()) {
				rt.opsForHash()
					.put("imgboard:meta", file.getFullName(), file.getUploadPath() + "|" + file.getUploadThumbPath());
			} else {
				rt.opsForHash()
					.put("imgboard:toDelete", file.getFullName(),
						file.getUploadPath() + "|" + file.getUploadThumbPath());
			}
		});

		log.info("Redis Caching Complete, cost {} s", (System.currentTimeMillis() - startTime) / 1000.0);
		log.info("==========================================================");

	}
}