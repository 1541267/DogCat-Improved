package com.community.dogcat.domain;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.ColumnDefault;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "img_board", schema = "dogcat")
public class ImgBoard {
	@Id
	@Size(max = 255)
	@Column(name = "file_uuid", nullable = false)
	private String fileUuid;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_no", nullable = false)
	private Post postNo;

	@Size(max = 50)
	@NotNull
	@Column(name = "file_name", nullable = false, length = 50)
	private String fileName;

	@NotNull
	@Column(name = "upload_time", nullable = false)
	@ColumnDefault("CURRENT_TIMESTAMP")
	private Instant uploadTime;

	// 저장 경로 저장하기위해 수정 - ys
	@Column(name = "upload_path")
	private String uploadPath;

	// 이미지 유무 확인 - ys
	@Column(name = "img")
	private boolean img;

	// 파일 확장자 저장
	@Column(name = "extension")
	private String extension;

	// 섬네일 경로 저장
	@Column(name = "thumbnailPath")
	private String thumbnailPath;

	public void setPostNo(Post post) {

	}
}