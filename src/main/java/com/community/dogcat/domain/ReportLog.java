package com.community.dogcat.domain;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.ColumnDefault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "report_log", schema = "dogcat")
public class ReportLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_no", nullable = false)
	private Long reportNo;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_no")
	private Post postNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reply_no")
	private Reply replyNo;

	@Size(max = 20)
	@NotNull
	@Column(name = "report_title", nullable = false, length = 20)
	private String reportTitle;

	@Size(max = 255)
	@NotNull
	@Column(name = "report_content", nullable = false)
	private String reportContent;

	@NotNull
	@Column(name = "reg_date", nullable = false)
	@ColumnDefault("CURRENT_TIMESTAMP")
	private Instant regDate;

}