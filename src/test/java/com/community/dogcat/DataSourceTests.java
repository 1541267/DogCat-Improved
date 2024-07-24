package com.community.dogcat;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class DataSourceTests {

	@Autowired
	private DataSource dataSource;

	@Test
	public void testConnection() throws SQLException {
		@Cleanup
		Connection con = dataSource.getConnection();
		log.info(con);

		Assertions.assertNotNull(con);
	}
}
