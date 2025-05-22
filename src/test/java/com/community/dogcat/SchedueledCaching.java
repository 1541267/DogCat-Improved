package com.community.dogcat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.util.scheduled.Caching;

@SpringBootTest
public class SchedueledCaching {

	@Autowired
	private Caching caching;

	@Test
	public void cachingTest() {

		caching.caching();

	}

}
