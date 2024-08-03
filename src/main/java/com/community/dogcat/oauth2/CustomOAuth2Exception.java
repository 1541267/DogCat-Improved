package com.community.dogcat.oauth2;

import org.springframework.security.core.AuthenticationException;

public class CustomOAuth2Exception extends AuthenticationException {

	public CustomOAuth2Exception(String message) {
		super(message);
	}

}
