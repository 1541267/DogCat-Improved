package com.community.dogcat.dto.user;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailDTO {

	private String userId;
	private String password;
	private String userName;
	private String nickname;
	private String tel;
	private boolean social;
	private Instant regDate;
}
