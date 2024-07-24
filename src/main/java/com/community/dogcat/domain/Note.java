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

import lombok.Getter;

@Getter
@Entity
@Table(name = "note", schema = "dogcat")
public class Note {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "note_no", nullable = false)
	private Long noteNo;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "from_id", nullable = false)
	private User fromId;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "to_id", nullable = false)
	private User toId;

	@Size(max = 1000)
	@NotNull
	@Column(name = "note_content", nullable = false, length = 1000)
	private String noteContent;

	@NotNull
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "reg_date", nullable = false)
	private Instant regDate;

	@Column(name = "read_date")
	@ColumnDefault("NULL")
	private Instant readDate;

}
