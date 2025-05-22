package com.community.dogcat.util.scheduled;

import java.util.Set;

import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Caching {

	@Autowired
	private RedisTemplate<String, String> rt;

	@Autowired
	private UploadRepository uploadRepository;

	/** 50분에 toDelete 파일 정리 후 */
	@Scheduled(cron = "0 51 23 * * *", zone = "Asia/Seoul")
	public void caching() {
		log.info("==========================================================");
		log.info("Caching After ToDelete : Start");
		rt.delete("imgboard:meta");

		Set<FileInfoDTO> fileInfos = uploadRepository.findFileUuidAndExtensionAndUploadTimeAndDeletePossibleByDeletePossibleFalse();

		fileInfos.forEach(dto -> {
			rt.opsForHash()
				.put("imgboard:meta", dto.getFullName(), dto.getUploadPath() + "|" + dto.getUploadThumbPath());
		});

		log.info("Caching After ToDelete : Complete");
		log.info("==========================================================");
	}
}
