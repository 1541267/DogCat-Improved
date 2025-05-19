package com.community.dogcat;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling  // 업로드 파일 체크 스케줄
@SpringBootApplication(exclude = {            // AWS S3 실행 경고 무시
	org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration.class,
	org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration.class,
	org.springframework.cloud.aws.autoconfigure.context.ContextRegionProviderAutoConfiguration.class})

public class DogCatApplication implements CommandLineRunner {

	@Value("${tempUploadPath}thumbnail")
	private String tempThumbPath;

	@Value("${finalUploadPath}thumbnail")
	private String uploadThumbPath;

	public static void main(String[] args) {
		SpringApplication.run(DogCatApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Files.createDirectories(Paths.get(tempThumbPath));
		Files.createDirectories(Paths.get(uploadThumbPath));
	}
}
