package com.community.dogcat.util.scheduled;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class Caching {

	private final RedisTemplate<String, Long> redisTemplate;
	private final ConcurrentMap<String, LongAdder> buffer = new ConcurrentHashMap<>();

	/** 애플리케이션에서 호출 **/
	public void increment(String key) {
		buffer.computeIfAbsent(key, k -> new LongAdder()).increment();
	}

	/** 5초마다 모은 값을 Redis에 파이프라인으로 한 번에 전송 **/
	@Scheduled(fixedRate = 5000)
	public void flushToRedis() {
		if (buffer.isEmpty()) return;

		redisTemplate.executePipelined((RedisCallback<Object>)conn -> {
			buffer.forEach((key, adder) -> {
				conn.incrBy(key.getBytes(), adder.longValue());
			});
			return null;
		});
		buffer.clear();
	}
}
