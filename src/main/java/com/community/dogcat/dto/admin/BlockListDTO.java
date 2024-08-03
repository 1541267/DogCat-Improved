package com.community.dogcat.dto.admin;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BlockListDTO {

	private String userId;

	private String userName;

	private Instant regDate;

	private boolean block;

	private String nickname;

	private boolean userVet;


}
