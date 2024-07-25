package com.community.dogcat.dto.user;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailDTO {

	private String userId;
	private String userPw;
	private String userName;
	private String nickname;
	private String tel;
	private boolean social;
	private boolean userVet;
	private Instant regDate;

}
