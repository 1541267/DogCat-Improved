package com.community.dogcat.dto.home;

import java.time.Instant;
import java.util.List;

import com.community.dogcat.domain.ImgBoard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeTodayListDTO {

	private Long postNo;

	private String userId;

	private String nickname;

	private Long exp;

	private boolean userVet;

	private String boardCode;

	private String postTitle;

	private String postContent;

	private Instant regDate;

	private Instant modDate;

	private String postTag;

	private boolean secret;

	private Long viewCount;

	private List<ImgBoard> imgBoards;

}
