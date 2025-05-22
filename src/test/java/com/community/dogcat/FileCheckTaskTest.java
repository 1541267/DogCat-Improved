package com.community.dogcat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.util.scheduled.FileCheckTask;

@SpringBootTest
public class FileCheckTaskTest {

	@Autowired private FileCheckTask fileCheckTask;

	@Test
	public void dailyFileCheck() throws Exception {
		fileCheckTask.checkFiles();
	}

	@Test
	public void toDelete() throws IOException {
		fileCheckTask.toDelete();
	}
}
