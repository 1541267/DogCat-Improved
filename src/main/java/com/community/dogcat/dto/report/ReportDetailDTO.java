package com.community.dogcat.dto.report;

import java.time.Instant;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReportDetailDTO {
	//User에서 가져옴
	private User userId;

	private String userName;

	private String nickname;

	//신고 ReportLog
	private Long reportNo;

	private String reportTitle;

	private String reportContent;

	//신고글 작성일
	private Instant regDate;

	//게시글 post
	private Post postNo;

	private String postTitle;

	//댓글 reply
	private Reply replyNo;

	private String replyContent;

}
