package com.community.dogcat.service.board.scrap;

import com.community.dogcat.domain.PostLike;
import com.community.dogcat.domain.Scrap;
import com.community.dogcat.dto.board.scrap.ScrapDTO;

public interface ScrapService {

	Long register(ScrapDTO scrapDTO);

	void delete(Long scrapNo, String userId);
}
