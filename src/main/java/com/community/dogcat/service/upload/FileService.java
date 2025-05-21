package com.community.dogcat.service.upload;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.community.dogcat.repository.upload.UploadRepository;

@Service
// 레디스 도입으로 인한 추가적인 파일 처리 로직
public class FileService {
	private static final String DELETE_QUEUE = "file:toDelete";

	private final UploadRepository repo;
	private final RedisTemplate<String, String> redis;
	private final FileStorageService storage;

	public FileService(UploadRepository repo, RedisTemplate<String, String> redis, FileStorageService storage) {
		this.repo = repo;
		this.redis = redis;
		this.storage = storage;
	}

}
