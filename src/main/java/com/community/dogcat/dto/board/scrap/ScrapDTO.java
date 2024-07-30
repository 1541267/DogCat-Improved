package com.community.dogcat.dto.board.scrap;

import com.community.dogcat.domain.Scrap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapDTO {

	private Long scrapNo;

	// 스크랩 하는 회원 정보
	private String userId;
	// 해당 게시글 번호
	private Long postNo;

}
