package com.community.dogcat.dto.sample.search;

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
public class AllSearchDTO {

	private Long postNo;

	// 게시글 작성자 아이디
	private String userId;

	// 게시글 작성자 닉네임
	private String nickname;

	// 게시글 작성자 경험지
	private Long exp;

	// 게시글 작성자 면허 정보
	private boolean userVet;

	private String boardCode;

	private String postTitle;

	private String postContent;

	private Instant regDate;

	private Instant modDate;

	private String postTag;

	private boolean secret;

	private Long likeCount;

	private Long dislikeCount;

	private Long viewCount;

	private boolean replyAuth;

	// 게시물의 첨부파일 정보
	private List<ImgBoard> imgBoards;

	// 게시물에 달린 댓글 수
	private Long replyCount;

}
