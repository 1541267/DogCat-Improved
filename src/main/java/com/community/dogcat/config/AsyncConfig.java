package com.community.dogcat.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
// 개선, 비동기 I/O 오프로드`
public class AsyncConfig implements AsyncConfigurer {

	private static int cores = Runtime.getRuntime().availableProcessors(); // 12

	// ─── I/O‐Bound 전용 풀 ───
	// @Bean("ioExecutor")
	// public ThreadPoolTaskExecutor ioExecutor() {
	// 	ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
	// 	// 블로킹 I/O가 많으면 코어 수×(1 + 대기/실행) 공식을 적용
	// 	exec.setCorePoolSize(cores * 2);    // 24
	// 	exec.setMaxPoolSize(cores * 4);     // 48
	// 	exec.setQueueCapacity(10_000);         // 요청 폭주 시 적당히 대기
	// 	exec.setThreadNamePrefix("io-");
	// 	exec.initialize();
	// 	return exec;
	// }

	// ─── CPU‐Bound 전용 풀 ───
	// @Bean("cpuExecutor")
	// public ThreadPoolTaskExecutor cpuExecutor() {
	// 	ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
	//
	// 	// 순수 연산은 코어 수 내에서만 실행
	// 	exec.setCorePoolSize(cores);
	// 	exec.setMaxPoolSize(cores * 2);
	// 	exec.setQueueCapacity(2_000);
	// 	exec.setThreadNamePrefix("cpu-");
	// 	exec.initialize();
	// 	return exec;
	// }

	// // ─── I/O‐Bound 전용 풀 ───
	@Bean("ioExecutor")
	public ThreadPoolTaskExecutor ioExecutor() {
		ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
		int cores = Runtime.getRuntime().availableProcessors(); // 12
		// 블로킹 I/O가 많으면 코어 수×(1 + 대기/실행) 공식을 적용
		exec.setCorePoolSize(40);    // 24
		exec.setMaxPoolSize(60);     // 48
		exec.setQueueCapacity(5000);         // 요청 폭주 시 적당히 대기
		exec.setThreadNamePrefix("io-");
		exec.initialize();
		return exec;
	}

	// // ─── CPU‐Bound 전용 풀 ───
	@Bean("cpuExecutor")
	public ThreadPoolTaskExecutor cpuExecutor() {
		ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
		int cores = Runtime.getRuntime().availableProcessors(); // 12
		// 순수 연산은 코어 수 내에서만 실행
		exec.setCorePoolSize(14);
		exec.setMaxPoolSize(15);
		exec.setQueueCapacity(500);
		exec.setThreadNamePrefix("cpu-");
		exec.initialize();
		return exec;
	}

	@Override
	public Executor getAsyncExecutor() {
		return ioExecutor();
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}
}
