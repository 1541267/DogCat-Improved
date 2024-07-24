package com.community.dogcat.repository.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.community.dogcat.dto.myPage.activity.UserScrapsActivityDTO;

public interface ScrapSearch {

	// 마이 페이지 : 회원이 보관한 게시글 찾기
	Page<UserScrapsActivityDTO> scrapsListWithUser(String[] types, String keyword, Pageable pageable, String userId);
}
