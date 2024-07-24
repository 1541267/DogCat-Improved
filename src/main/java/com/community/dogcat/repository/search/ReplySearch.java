package com.community.dogcat.repository.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.community.dogcat.dto.myPage.activity.UserRepliesActivityDTO;

public interface ReplySearch {

	// 마이 페이지 : 회원이 작성한 댓글 찾기
	Page<UserRepliesActivityDTO> repliesListWithUser(String[] types, String keyword, Pageable pageable, String userId);
}
