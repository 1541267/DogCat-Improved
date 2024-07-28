package com.community.dogcat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

	// URL 요청 에러 잡기 위해 필터+로깅
	@Bean
	public CommonsRequestLoggingFilter logFilter(){
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);
		filter.setIncludeHeaders(true);
		filter.setAfterMessagePrefix("요청된 URL DATA: ");
		return filter;
	}
}


