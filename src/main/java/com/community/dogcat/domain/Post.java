package com.community.dogcat.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post", schema = "dogcat")
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_no", nullable = false)
	private Long postNo;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User userId;

	@NotNull
	@Column(name = "board_code", nullable = false, length = 50)
	private String boardCode;

	@Size(max = 100)
	@NotNull
	@Column(name = "post_title", nullable = false, length = 100)
	private String postTitle;

	// summernote 사진 업로드 하면 본문에 내용을 차지해서 길이 늘림
	@NotNull
	@Column(name = "post_content", nullable = false, columnDefinition = "TEXT")
	private String postContent;

	@Column(name = "reg_date", nullable = false)
	@ColumnDefault("CURRENT_TIMESTAMP")
	private Instant regDate;

	@Column(name = "mod_date")
	@ColumnDefault("NULL")
	private Instant modDate;

	@Size(max = 10)
	@ColumnDefault("NULL")
	@Column(name = "post_tag", length = 10)
	private String postTag;

	@Column(name = "secret", nullable = false)
	@ColumnDefault("false")
	private boolean secret;

	@Column(name = "like_count")
	@ColumnDefault("0")
	private Long likeCount;

	@Column(name = "dislike_count")
	@ColumnDefault("0")
	private Long dislikeCount;

	@Column(name = "view_count")
	@ColumnDefault("0")
	private Long viewCount;

	@ColumnDefault("false")
	@Column(name = "reply_auth", nullable = false)
	private boolean replyAuth;

	// 게시판 등록시 이미지도 참조
	@OneToMany(mappedBy = "postNo", cascade = CascadeType.ALL,
		orphanRemoval = true, fetch = FetchType.LAZY)
	private final List<ImgBoard> images = new ArrayList<>();

	// 게시글 삭제시 정보 삭제
	@OneToMany(mappedBy = "postNo", cascade = CascadeType.ALL,
		orphanRemoval = true)
	private Set<Reply> replies;

	@OneToMany(mappedBy = "postNo", cascade = CascadeType.ALL,
		orphanRemoval = true)
	private Set<Scrap> scraps;

	@OneToMany(mappedBy = "postNo", cascade = CascadeType.ALL,
		orphanRemoval = true)
	private Set<PostLike> postLikes;

	@OneToMany(mappedBy = "postNo", cascade = CascadeType.ALL,
		orphanRemoval = true)
	private Set<ReportLog> reports;

	// 게시글 수정
	public void modify(String boardCode, String postTitle, String postContent,
		Instant modDate, String postTag, Boolean secret, Boolean replyAuth) {
		this.boardCode = boardCode;
		this.postTitle = postTitle;
		this.postContent = postContent;
		this.modDate = modDate;
		this.postTag = postTag;
		this.secret = secret;
		this.replyAuth = replyAuth;
	}

	// 게시글 좋아요/싫어요 count
	public void count(Long likeCount, Long disLikeCount) {
		this.likeCount = likeCount;
		this.dislikeCount = disLikeCount;
	}
}
