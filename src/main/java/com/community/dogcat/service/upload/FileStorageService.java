package com.community.dogcat.service.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.community.dogcat.config.StorageProperties;

@Service
public class FileStorageService {

	private final Path rootLocation;

	public FileStorageService(StorageProperties prop) {
		this.rootLocation = Paths.get(prop.getRoot());
	}

	@PostConstruct
	public void init() throws IOException {
		Files.createDirectories(rootLocation);
	}
}
