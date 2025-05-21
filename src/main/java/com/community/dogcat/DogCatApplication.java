package com.community.dogcat;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

	@Value("${tempUploadPath}")
	private String tempPath;

	@Value("${finalUploadPath}")
	private String uploadPath;

	// 도커 부터 시작
	public static void main(String[] args) throws Exception {

		// 1) 기존 컨테이너가 있으면 시작(start), 없으면 up 으로 생성+시작
		try {
			runCommand("docker-compose", "start");
		} catch (IllegalStateException e) {
			// start에 실패하면(컨테이너가 없으면) create+up
			runCommand("docker-compose", "up", "-d");
		}

		// boot 기동
		SpringApplication.run(DogCatApplication.class, args);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				runCommand("docker-compose", "stop");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	}

	@Override
	public void run(String... args) throws Exception {
		// 날짜 별로 업로드 디렉토리 생성
		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		Files.createDirectories(Paths.get(tempPath));
		Files.createDirectories(Paths.get(uploadPath + today));
	}

	private static void runCommand(String... command) throws Exception {
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.inheritIO();  // 표준 입출력을 이 프로세스에 연결
		Process p = pb.start();
		int exitCode = p.waitFor();
		if (exitCode != 0) {
			throw new IllegalStateException(
				String.join(" ", command) + " failed with exit code " + exitCode
			);
		}
	}


}