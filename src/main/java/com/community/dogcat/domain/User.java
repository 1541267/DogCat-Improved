package com.community.dogcat.domain;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Builder
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", schema = "dogcat")
public class User {

	@Id
	@Column(name = "user_id", length = 50)
	private String userId;

	@Column(name = "user_pw", length = 100, nullable = false)
	private String userPw;

	@Column(name = "user_name", length = 20, nullable = false)
	private String userName;

	@Column(name = "nickname", length = 25, nullable = false)
	private String nickname;

	@Column(name = "tel", length = 20, nullable = false)
	private String tel;

	@Column(name = "reg_date", nullable = false, updatable = false)
	@ColumnDefault("CURRENT_TIMESTAMP")
	private Instant regDate;

	@Column(name = "mod_date")
	private Instant modDate;

	@Column(name = "exp", nullable = false)
	@ColumnDefault("0")
	private Long exp;

	@Column(name = "social", nullable = false)
	@ColumnDefault("false")
	private boolean social;

	@Column(name = "login_lock", nullable = false)
	@ColumnDefault("false")
	@Setter
	private boolean loginLock;

	@Column(name = "block", nullable = false)
	@ColumnDefault("false")
	private boolean block;

	@Column(name = "user_vet", nullable = false)
	@ColumnDefault("false")
	private boolean userVet;

	// exp 값을 증가시키는 메서드
	public void incrementExp() {

		if (this.exp == null) {

			this.exp = 0L;

		} else if (this.exp < 2999) {

			this.exp += 2;

		} else if (this.exp == 2999) {

			this.exp += 1;

		}

	}

	// exp 값을 증가시키는 메서드 (게시글 작성)
	public void incrementPostExp() {
		this.exp = (this.exp != null) ? (this.exp >= 3000 ? 3000 : this.exp + 1) : 0;
	}
}
