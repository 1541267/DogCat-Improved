package com.community.dogcat.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.community.dogcat.dto.board.scrap.ScrapDTO;
import com.community.dogcat.service.board.scrap.ScrapService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
class ScrapServiceImplTest {

	@Autowired
	private ScrapService scrapService;

	@Test
	public void testRegister() {
		ScrapDTO scrapDTO = ScrapDTO.builder()
			.userId("user1")
			.postNo(10L)
			.build();

		log.info(scrapService.register(scrapDTO));
	}

	// @Test
	// public void testDelete() {
	// 	Long scrapNo = 6L;
	// 	scrapService.delete(scrapNo);
	// }

	// @Test
	// void readOne() {
	// 	Long scrapNo = 2L;
	// 	scrapService.readOne(scrapNo);
	// 	log.info(scrapNo);
	// }
	//
	// @Test
	// public void testList() {
	// 	ScrapDTO scrapDTO = ScrapDTO.builder()
	// 		.userId("user1")
	// 		.postNo(10L)
	// 		.scrapNo(22L)
	// 		.build();
	//
	// 	List<ScrapDTO> scraps = scrapService.list(scrapDTO.getScrapNo());
	// 	scraps.forEach(scrap -> log.info("ScrapDTO: {}", scrap));
	// }
}