package com.community.dogcat.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class JoinDTO {

	private String userId;
	private String userPw;
	private String userName;
	private String nickname;
	private String tel;
	private boolean userVet;

}
