package com.community.dogcat.util.consumer;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.dto.uploadImage.ThumbnailRequestPayload;
import com.community.dogcat.service.util.async.ThumbnailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThumbnailKafkaConsumer {
	private final ThumbnailService thumbnailService;
	private final RedisTemplate<String, String> rt;

	@KafkaListener(topics = "thumbnail-Generator", groupId = "thumbnail-workers", concurrency = "14")
	public void consume(ThumbnailRequestPayload payload) {
		thumbnailService.createThumbnails(payload.getInfos(), payload.getBaseDir()).join();

		String[] files = payload.getInfos().stream()
			.map(FileInfoDTO::getFullName)
			.toArray(String[]::new);
		rt.opsForSet().remove("imgboard:thumbnail:processing", (Object[])files);
	}
}
