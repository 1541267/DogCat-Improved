package com.community.dogcat.util.Jdbc;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardJdbcTemplate {

	private final NamedParameterJdbcTemplate jdbc;

	public void batchUpdateViewCounts(Map<Long, Long> viewMap) {

		String sql = """
				UPDATE post SET view_count = :viewCount
				WHERE post_no = :postNo
			""";

		List<Map<String, Object>> batchValues = viewMap.entrySet().stream()
			.map(e -> Map.<String, Object>of(
				"postNo", e.getKey(),
				"viewCount", e.getValue()
			)).toList();

		jdbc.batchUpdate(sql, batchValues.toArray(new Map[0]));
	}

}
