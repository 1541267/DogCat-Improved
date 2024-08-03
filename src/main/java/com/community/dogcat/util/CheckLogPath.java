package com.community.dogcat.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CheckLogPath {

	@Value("${loggingPath}")
	private String loggingPath;

	@EventListener(ApplicationReadyEvent.class)
	public void checkDirectory() {
		File logDir = new File(loggingPath);

		if (!logDir.exists()){
			logDir.mkdir();
		}
	}
}
