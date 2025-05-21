package com.community.dogcat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.util.FileCheckTask;

@SpringBootTest
public class FileCheckTaskTest {

	@Autowired private FileCheckTask fileCheckTask;

	@Test
	public void monthlyFileCheck() throws Exception {
		fileCheckTask.checkFiles();
	}
}
