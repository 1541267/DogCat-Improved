package com.community.dogcat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.util.scheduled.BoardCacheRefresh;

import groovy.util.logging.Slf4j;

@Slf4j
@SpringBootTest
public class BoardCacheRefreshTest {

	@Autowired
	private BoardCacheRefresh boardCacheRefresh;

	@Test
	public void viewUpdator() {
		boardCacheRefresh.updateViewCount();

	}

}
