package com.community.dogcat.dto.admin;

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
public class ReportListDTO {
	//User에서 가져옴
	private User user;

	private String nickname;

	//ReportLog에서 가져옴

	private Long reportNo;

	private User reportedUser;

	private String reportTitle;

	private String reportContent;

	private Instant regDate;

	private Post post;

	private Reply reply;

}
