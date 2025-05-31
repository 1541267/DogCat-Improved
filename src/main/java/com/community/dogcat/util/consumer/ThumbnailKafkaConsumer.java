package com.community.dogcat.util.consumer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
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

	// 배치 메세지 처리
	@KafkaListener(topics = "thumbnail-Generator", groupId = "thumbnail-workers", concurrency = "14", containerFactory = "kafkaBatchListenerContainerFactory")
	public void consume(List<ThumbnailRequestPayload> payloads, Acknowledgment ack) {

		// 배치 단위로 오프셋만 먼저 커밋(=Ack)
		ack.acknowledge();

		List<CompletableFuture<Void>> futures = payloads.stream()
			.map(payload ->
				thumbnailService.createThumbnails(payload.getInfos(), payload.getBaseDir())
					.thenRun(() -> {
						// Redis 처리 완료 표시
						String[] files = payload.getInfos().stream()
							.map(FileInfoDTO::getFullName)
							.toArray(String[]::new);
						rt.opsForSet().remove("imgboard:thumbnail:processing", (Object[])files);
					})
			)
			.toList();

		// 배치 내 모든 메시지 처리가 끝날 때까지 대기
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	}
	// // 배치 메세지 처리
	// @KafkaListener(topics = "thumbnail-Generator", groupId = "thumbnail-workers", concurrency = "14", containerFactory = "kafkaBatchListenerContainerFactory")
	// public void consume(ThumbnailRequestPayload payload, Acknowledgment ack) {
	// 	try {
	// 		// 1) 썸네일 생성
	// 		thumbnailService.createThumbnails(payload.getInfos(), payload.getBaseDir()).join();
	//
	// 		// 2) Redis에서 처리 완료 표시
	// 		String[] files = payload.getInfos().stream()
	// 			.map(FileInfoDTO::getFullName)
	// 			.toArray(String[]::new);
	// 		rt.opsForSet().remove("imgboard:thumbnail:processing", (Object[])files);
	//
	// 		// 3) 정상 처리 시에만 한 번 커밋
	// 		ack.acknowledge();
	//
	// 	} catch (Exception ex) {
	// 		// 예외가 나도 컨슈머 스레드를 죽이지 않고, error 로그만 남겨 놓는다.
	// 		log.error("썸네일 처리 중 예외 발생, payload=[{}], error=[{}]",
	// 			payload, ex.toString());
	// 	}
	// }

	// 단일 메시지 처리
	// private final ThumbnailService thumbnailService;
	// private final RedisTemplate<String, String> rt;
	//
	// @KafkaListener(topics = "thumbnail-Generator", groupId = "thumbnail-workers", concurrency = "14")
	// public void consume(ThumbnailRequestPayload payload) {
	// 	thumbnailService.createThumbnails(payload.getInfos(), payload.getBaseDir()).join();
	//
	// 	String[] files = payload.getInfos().stream()
	// 		.map(FileInfoDTO::getFullName)
	// 		.toArray(String[]::new);
	// 	rt.opsForSet().remove("imgboard:thumbnail:processing", (Object[])files);
	// }
}
