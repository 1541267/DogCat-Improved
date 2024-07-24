package com.community.dogcat.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {

	private String userId;
	private String userName;
	private String role;

}
