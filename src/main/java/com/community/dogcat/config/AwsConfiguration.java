package com.community.dogcat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class AwsConfiguration {

	// AmazonS3Client는 deprecated됨 -> AmazonS3로 사용
	@Bean
	public AmazonS3 amazonS3() {
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials
			("AKIAXYKJRGBHYYY6NUKK", "h2okAqQtVZxuZDJvPWyAZs8mfLDMyLiP2bgcQ6Yn");

		return AmazonS3ClientBuilder.standard()
			.withRegion("ap-northeast-2")
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
			.build();

	}
}
