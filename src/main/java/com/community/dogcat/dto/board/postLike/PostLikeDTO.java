package com.community.dogcat.dto.board.postLike;

import com.community.dogcat.domain.PostLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeDTO {

	// 게시글 정보
	private Long postNo;

	private String userId;

	private Long likeCount;

	private Long dislikeCount;

	// 추천 정보
	private Long likeNo;

	private boolean likeState;

	private boolean dislikeState;

}
