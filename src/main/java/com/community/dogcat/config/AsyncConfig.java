package com.community.dogcat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
// 개선, 비동기 I/O 오프로드`
public class AsyncConfig {

	@Bean
	public ThreadPoolTaskExecutor fileTaskExecutor() {
		ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
		exec.setCorePoolSize(4);
		exec.setMaxPoolSize(10);
		exec.setQueueCapacity(200);
		exec.setThreadNamePrefix("file-io-");
		exec.initialize();
		return exec;
	}

}
