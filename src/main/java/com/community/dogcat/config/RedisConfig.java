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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.community.dogcat.dto.board.post.PostReadDTO;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
		// 기본 String 직렬화 사용 (k, v), 사용하지 않을 시
		tpl.setKeySerializer(new StringRedisSerializer());
		tpl.setHashKeySerializer(new StringRedisSerializer());
		tpl.setValueSerializer(new StringRedisSerializer());
		tpl.setHashValueSerializer(new StringRedisSerializer());
		return tpl;
	}

	// 게시글 캐싱
	@Bean("redisTemplatePostReadDTO")
	public RedisTemplate<String, PostReadDTO> redisTemplate(LettuceConnectionFactory cf) {
		// PostReadDTO의 Instant 에러, jackson 설정엔 Instant 를 직렬/역직렬 모듈이 없음, 이를 지정
		// 1) ObjectMapper 생성 및 JavaTimeModule 등록
		ObjectMapper om = new ObjectMapper();
		om.registerModule(new JavaTimeModule());
		// Instant 등을 ISO-8601 문자열로 저장하도록 설정
		om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// 2) 이 Mapper를 사용하는 GenericJackson2JsonRedisSerializer 생성
		Jackson2JsonRedisSerializer<PostReadDTO> serializer = new Jackson2JsonRedisSerializer<>(PostReadDTO.class);
		serializer.setObjectMapper(om);

		// 3) RedisTemplate 설정
		RedisTemplate<String, PostReadDTO> rt = new RedisTemplate<>();
		rt.setConnectionFactory(cf);
		rt.setKeySerializer(new StringRedisSerializer());
		rt.setHashKeySerializer(new GenericToStringSerializer<>(Long.class));
		rt.setValueSerializer(serializer);
		rt.setHashValueSerializer(serializer);
		rt.afterPropertiesSet();
		return rt;
	}

	// 게시글 캐싱
	@Bean("redisTemplateSL")
	public RedisTemplate<String, Long> counterRedisTemplate(LettuceConnectionFactory cf) {
		RedisTemplate<String, Long> rt = new RedisTemplate<>();
		rt.setConnectionFactory(cf);

		// key = String, hashKey = long 직렬화
		rt.setKeySerializer(new StringRedisSerializer());
		rt.setHashKeySerializer(new GenericToStringSerializer<>(Long.class));

		// value, hashValue: Long → String 직렬화
		GenericToStringSerializer<Long> longSer = new GenericToStringSerializer<>(Long.class);
		rt.setValueSerializer(longSer);
		rt.setHashValueSerializer(longSer);

		rt.afterPropertiesSet();
		return rt;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory cf) {
		// RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
		// 	.disableCachingNullValues();
		// 1) ObjectMapper 설정
		ObjectMapper objectMapper = new ObjectMapper();
		// Java 8 날짜/시간 처리
		objectMapper.registerModule(new JavaTimeModule());
		// Hibernate 프록시 처리
		objectMapper.registerModule(new Hibernate5Module());
		// 타입 메타데이터 추가 (NON_FINAL: 최종 클래스가 아닌 모든 클래스에 포함)
		objectMapper.activateDefaultTyping(
			LaissezFaireSubTypeValidator.instance,
			ObjectMapper.DefaultTyping.NON_FINAL,
			JsonTypeInfo.As.PROPERTY
		);
		// ISO-8601 포맷 사용
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// 2) Redis Serializer 생성
		GenericJackson2JsonRedisSerializer redisSerializer =
			new GenericJackson2JsonRedisSerializer(objectMapper);

		// 3) Cache 설정
		RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer)
			);

		// 4) CacheManager 빌드
		return RedisCacheManager.builder(cf)
			.cacheDefaults(cacheConfig)
			.build();
	}
}


