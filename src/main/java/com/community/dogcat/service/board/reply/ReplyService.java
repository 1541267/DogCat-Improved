package com.community.dogcat.service.board.reply;

import java.util.List;

import com.community.dogcat.domain.Reply;
import com.community.dogcat.dto.board.reply.ReplyDTO;

public interface ReplyService {

	Long register(ReplyDTO replyDTO);

	void delete(Long replyNo, String userId);

	List<ReplyDTO> getListOfReply(Long postNo);

	Reply findReplyByReplyNo(Long replyNo);
}
