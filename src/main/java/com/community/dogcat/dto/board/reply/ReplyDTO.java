package com.community.dogcat.dto.board.reply;

import java.util.Date;

import com.community.dogcat.domain.Reply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDTO {

	private Long replyNo;

	// 댓글을 다는 회원 정보
	private String userId;

	private String nickname;

	private Long postNo;

	private String replyContent;

	private Date regDate;

	// Entity -> DTO
	public ReplyDTO(Reply reply) {
		this.replyNo = reply.getReplyNo();
		this.replyContent = reply.getReplyContent();
		this.regDate = reply.getRegDate();
		this.userId = reply.getUserId().getUserId();
		this.nickname = reply.getUserId().getNickname();
		this.postNo = reply.getPostNo().getPostNo();
	}
}
