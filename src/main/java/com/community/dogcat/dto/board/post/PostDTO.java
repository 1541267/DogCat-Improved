package com.community.dogcat.dto.board.post;

import java.time.Instant;

import com.community.dogcat.domain.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

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

	// Entity -> DTO
	public PostDTO(Post post) {
		this.postNo = post.getPostNo();
		this.userId = post.getUserId().getUserId();
		this.nickname = post.getUserId().getNickname();
		this.exp = post.getUserId().getExp();
		this.userVet = post.getUserId().isUserVet();
		this.boardCode = post.getBoardCode();
		this.postTitle = post.getPostTitle();
		this.postContent = post.getPostContent();
		this.regDate = post.getRegDate();
		this.modDate = post.getModDate();
		this.postTag = post.getPostTag();
		this.secret = post.isSecret();
		this.likeCount = post.getLikeCount();
		this.dislikeCount = post.getDislikeCount();
		this.viewCount = post.getViewCount();
		this.replyAuth = post.isReplyAuth();
	}
}
