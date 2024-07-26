package com.community.dogcat.dto.admin;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserDetailDTO {

	private String userId;

	private String userName;

	private Instant regDate;

	private String nickname;

	private boolean userVet;

	private String authorities;



}
