package com.community.dogcat.service.board.scrap;

import com.community.dogcat.dto.board.scrap.ScrapDTO;

public interface ScrapService {

	Long register(ScrapDTO scrapDTO);

	void delete(Long scrapNo, String userId);

	// ScrapDTO readOne(Long scrapNo);
	//
	// List<ScrapDTO> list(Long scrapNo);
}
