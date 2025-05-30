package com.community.dogcat.util.cache;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.community.dogcat.dto.board.post.PostReadDTO;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostCache {

	private final RedisTemplate<String, PostReadDTO> redisTemplate;
	private final RedisTemplate<String, Long> counterRedisTemplate;

	/** 게시글 상세보기 시 사용 할 조회 수 캐싱
	 * hashKey = Long postNo
	 * value = db 반환 Long 값 (개수)
	 * */
	public Long getViewCountAndIncrement(Long postNo, Supplier<Long> dbCountSupplier) {
		HashOperations<String, String, Long> hashOps = counterRedisTemplate.opsForHash();
		String field = postNo.toString();

		if (hashOps.hasKey("post:views", field)) {
			// 이미 있으면 INCR
			return hashOps.increment("post:views", field, 1);
		} else {
			// 없으면 DB에서 초기값 + 증분
			Long base = dbCountSupplier.get();
			Long updated = base + 1;
			hashOps.put("post:views", field, updated);
			return updated;
		}
	}

	/** 게시글 상세보기 시 사용 할 캐싱
	 * hashKey = Long postNo
	 * value = db 반환 Long 값 (개수)
	 * */
	public Long getCachedCount(String hash, Long postNo, Supplier<Long> dbCount) {
		HashOperations<String, String, Long> hashOps =
			counterRedisTemplate.opsForHash();
		String field = postNo.toString();
		if (hashOps.hasKey(hash, field)) {
			hashOps.put(hash, field, dbCount.get());
			return dbCount.get();
		} else {
			Long cnt = dbCount.get();
			hashOps.put(hash, field, cnt);
			return cnt;
		}
	}

	public void evictPostCache(Long postNo) {

		String field = postNo.toString();

		List<String> hashes = List.of(
			"post:views",
			"post:replies",
			"post:likes",
			"post:dislikes"
		);

		redisTemplate.executePipelined((RedisCallback<Object>)conn -> {
			conn.hDel("post:content".getBytes(StandardCharsets.UTF_8), field.getBytes(StandardCharsets.UTF_8));
			return null;
		});

		counterRedisTemplate.executePipelined((RedisCallback<Object>)conn -> {
			for (String hash : hashes) {
				conn.hDel(hash.getBytes(), field.getBytes());
			}
			return null;
		});
	}
}
