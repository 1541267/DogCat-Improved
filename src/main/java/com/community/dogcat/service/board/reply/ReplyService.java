package com.community.dogcat.service.board.reply;

import java.util.List;

import com.community.dogcat.dto.board.reply.ReplyDTO;

public interface ReplyService {

	Long register(ReplyDTO replyDTO);

	ReplyDTO read(Long replyNo);

	void delete(Long replyNo, String userId);

	List<ReplyDTO> getListOfReply(Long postNo);
}
