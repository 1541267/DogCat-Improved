package com.community.dogcat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


// 개선,
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file.storage")
public class StorageProperties {

	private String root;

}
