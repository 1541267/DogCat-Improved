package com.community.dogcat.service.board.postLike;

import com.community.dogcat.domain.PostLike;
import com.community.dogcat.domain.Scrap;
import com.community.dogcat.dto.board.postLike.PostLikeDTO;

public interface PostLikeService {

	Long register(PostLikeDTO postReadDTO);

	void delete(Long likeNo, String userId);
}
