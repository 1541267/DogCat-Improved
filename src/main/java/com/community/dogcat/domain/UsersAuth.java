package com.community.dogcat.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Table(name = "users_auth", schema = "dogcat")
public class UsersAuth {
	@Id
	@Size(max = 50)
	@Column(name = "user_id", nullable = false, length = 50)
	private String userId;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
	private User users;

	@Size(max = 100)
	@ColumnDefault("'ROLE_USER'")
	@Column(name = "authorities", nullable = false, length = 100)
	private String authorities;

}