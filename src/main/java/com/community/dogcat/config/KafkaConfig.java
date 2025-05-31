package com.community.dogcat.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.community.dogcat.dto.uploadImage.ThumbnailRequestPayload;

@Configuration
public class KafkaConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	/**
	 * 1) ConsumerFactory를 별도로 정의 (키=String, 값=ThumbnailRequestPayload(Json) 디시리얼라이즈)
	 */
	@Bean
	public ConsumerFactory<String, ThumbnailRequestPayload> consumerFactory() {
		JsonDeserializer<ThumbnailRequestPayload> deserializer =
			new JsonDeserializer<>(ThumbnailRequestPayload.class);
		deserializer.addTrustedPackages("com.community.dogcat.dto"); // DTO가 있는 패키지

		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "thumbnail-workers");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);

		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
	}

	/**
	 * 2) ConcurrentKafkaListenerContainerFactory 에서 배치 모드를 활성화(setBatchListener(true))
	 */
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, ThumbnailRequestPayload> kafkaBatchListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, ThumbnailRequestPayload> factory
			= new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory());

		// 배치 모드 활성화
		// 이 설정이 있어야, @KafkaListener 메서드를 List<ThumbnailRequestPayload> 형태의 파라미터로 받을 수 있음
		factory.setBatchListener(true);

		// (Optional) 병렬 컨슈밍을 위한 컨커런시 설정(예: 14로 유지)
		factory.setConcurrency(14);
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
		// (Optional) 메시지 리스너가 배치 메시지를 받을 때, record 리스트 크기만큼 한 번에 호출되기 때문에
		// 메시지를 처리하다 예외가 날 경우 배치 전체가 롤백될 수 있음을 염두에 둡니다.
		return factory;
	}
}