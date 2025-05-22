package com.community.dogcat.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// 개선, 조회, 삭제 부하를 줄이기 위한 redis 도입
@EnableCaching
@Configuration
public class RedisConfig {

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		// application.yml 설정을 바탕으로 LettuceConnectionFactory 자동 생성
		return new LettuceConnectionFactory();
	}

	@Bean
	@Primary
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory rf) {

		RedisTemplate<String, String> tpl = new RedisTemplate<>();
		tpl.setConnectionFactory(rf);
		// 기본 String 직렬화 사용 (k, v)
		tpl.setKeySerializer(new StringRedisSerializer());
		tpl.setHashKeySerializer(new StringRedisSerializer());
		return tpl;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory cf) {
		RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
			.disableCachingNullValues();

		// 캐시 ttl 사용 x
		// Map<String, RedisCacheConfiguration> configs = Map.of(
		// 	"imgBoard:meta", cfg.entryTtl(Duration.ofMinutes(10)),
		// 	"imgBoard:toDelete", cfg.entryTtl(Duration.ofMinutes(5))
		// );

		return RedisCacheManager.builder(cf)
			.cacheDefaults(cfg).build();
	}

}


