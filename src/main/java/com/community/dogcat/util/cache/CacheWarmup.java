package com.community.dogcat.util.cache;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.Post;
import com.community.dogcat.dto.board.post.PostReadDTO;
import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.board.postLike.PostLikeRepository;
import com.community.dogcat.repository.board.reply.ReplyRepository;
import com.community.dogcat.repository.upload.UploadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class CacheWarmup implements ApplicationListener<ApplicationReadyEvent> {

	private final UploadRepository uploadRepository;
	private final RedisTemplate<String, String> rt;
	private final BoardRepository boardRepository;
	private final ReplyRepository replyRepository;
	private final PostLikeRepository postLikeRepository;
	private final UploadedImageCaching uploadedImageCaching;

	private final RedisTemplate<String, PostReadDTO> redisTemplate;
	private final RedisTemplate<String, Long> counterRedisTemplate;

	private final String bigLogLine = "===========================================";
	private final String smolLogLine = "-------------------------------------------";

	/** 애플리케이션 시작 직후 한 번 실행,
	 * 애플리케이션 시작 시 모든 이미지 메타데이터를 레디스에 캐싱 */
	public void preloadImageCache() {

		List<FileInfoDTO> files = uploadRepository.listFileInfoDTOByDeletePossibleFalse();
		List<FileInfoDTO> deletedFiles = uploadRepository.listFindFileInfoDTOByDeletePossibleTrue();
		log.info(smolLogLine);
		log.info("Image Cache WarmUp...");

		// 이미지 캐시웜업 파이프라이닝
		uploadedImageCaching.cacheMetadataAddOrDelete(files, deletedFiles);
	}

	/** 게시글 캐싱
	 content, dislikes, likes, replies, views*/
	private void preloadPostCache() {

		log.info(smolLogLine);
		log.info("Post Cache WarmUp...");

		List<Long> posts = boardRepository.findAllPostNo();

		Map<Long, Long> viewMap = boardRepository.findAllById(posts).stream()
			.collect(Collectors.toMap(Post::getPostNo, Post::getViewCount));

		// 댓글 수
		Map<Long, Long> replyMap = posts.stream()
			.collect(Collectors.toMap(
				id -> id,
				replyRepository::countRepliesByPost
			));

		// 좋아요/싫어요 수
		Map<Long, Long> likeMap = posts.stream()
			.collect(Collectors.toMap(
				id -> id,
				postLikeRepository::countByPostNoAndIsLikeTrue
			));
		Map<Long, Long> dislikeMap = posts.stream()
			.collect(Collectors.toMap(
				id -> id,
				postLikeRepository::countByPostNoAndIsLikeFalse
			));

		// 3) 파이프라이닝으로 카운터 해시에 HSET
		counterRedisTemplate.executePipelined((RedisCallback<Object>)conn -> {
			byte[] field, hash;
			for (Long postNo : posts) {
				field = postNo.toString().getBytes(StandardCharsets.UTF_8);

				// 조회수
				// hash = "post:views".getBytes(StandardCharsets.UTF_8);
				// conn.hSet(hash, field, viewMap.get(postNo).toString().getBytes(StandardCharsets.UTF_8));

				// 댓글 수
				hash = "post:replies".getBytes(StandardCharsets.UTF_8);
				conn.hSet(hash, field, replyMap.get(postNo).toString().getBytes(StandardCharsets.UTF_8));

				// 좋아요
				hash = "post:likes".getBytes(StandardCharsets.UTF_8);
				conn.hSet(hash, field, likeMap.get(postNo).toString().getBytes(StandardCharsets.UTF_8));

				// 싫어요
				hash = "post:dislikes".getBytes(StandardCharsets.UTF_8);
				conn.hSet(hash, field, dislikeMap.get(postNo).toString().getBytes(StandardCharsets.UTF_8));
			}
			return null;
		});

		// 4) 파이프라이닝으로 정적 데이터 캐시 (PostReadDTO) 웜업
		redisTemplate.executePipelined((RedisCallback<Object>)conn -> {
			byte[] hash = "post:content".getBytes(StandardCharsets.UTF_8);
			byte[] field;
			byte[] value;

			RedisSerializer<PostReadDTO> dtoSer =
				(RedisSerializer<PostReadDTO>)redisTemplate.getHashValueSerializer();

			for (Long postNo : posts) {
				Post post = boardRepository.findById(postNo).get();
				PostReadDTO dto = new PostReadDTO(post);

				field = postNo.toString().getBytes(StandardCharsets.UTF_8);
				value = dtoSer.serialize(dto);  // 해시값 직렬화기 사용

				conn.hSet(hash, field, value);
			}
			return null;
		});

	}

	@Override
	@Transactional(readOnly = true)
	public void onApplicationEvent(ApplicationReadyEvent event) {
		log.info(bigLogLine);
		log.info("Redis Caching...");
		long startTime = System.currentTimeMillis();

		preloadImageCache();
		preloadPostCache();

		log.info("Redis Caching Complete, cost {} s", (System.currentTimeMillis() - startTime) / 1000.0);
		log.info("==========================================================");

	}

}
