package com.community.dogcat.util.scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.transaction.Transactional;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.community.dogcat.util.Jdbc.BoardJdbcTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BoardCacheRefresh {

	private final RedisTemplate<String, Long> rt;
	// JdbcTemplete
	private final BoardJdbcTemplate boardJdbcTemplate;

	// 수정된 게시글 ID를 임시로 보관하는 큐
	private final BlockingQueue<Long> postUpdateQueue = new LinkedBlockingQueue<>();

	private final String bigLogLine = "===========================================";
	private final String smolLogLine = "-------------------------------------------";

	@Transactional
	@Scheduled(cron = "*/30 * * * * *")
	/** 30초마다 조회수 업데이트 */
	public void updateViewCount() {

		// log.info(bigLogLine);

		String key = "post:views";
		String processingKey = "post:views:processing";

		if (rt.opsForHash().size(key) == 0) {
			// log.info("조회수 동기화 할 게시글이 없습니다");
			// log.info(bigLogLine);
			return;
		}

		rt.rename(key, processingKey);

		HashOperations<String, Long, Long> ops = rt.opsForHash();
		Map<Long, Long> viewMap = ops.entries(processingKey);

		boardJdbcTemplate.batchUpdateViewCounts(viewMap);
		viewMap.keySet().forEach(postNo ->
			rt.opsForHash().delete("post:content", postNo));

		rt.delete(processingKey);

		// log.info("조회수 동기화 완료");
		// log.info(bigLogLine);
	}

	// 업데이트 된 게시글 큐에 삽입
	public void markPostUpdated(Long postNo) {

		postUpdateQueue.add(postNo);

	}
	
	
	
	@Scheduled(fixedDelay = 250)
	public void updateContent() {

		List<Long> toEvict = new ArrayList<>();

		postUpdateQueue.drainTo(toEvict);

		if (!toEvict.isEmpty()) {
			rt.executePipelined((RedisCallback<Object>)conn -> {

				byte[] rawKey = "post:content".getBytes();

				for (Long id : toEvict) {
					byte[] field = rt.getStringSerializer().serialize(id.toString());
					conn.hDel(rawKey, field);
				}
				return null;
			});
		}
	}
}
