package com.community.dogcat.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JWTUtil {

	private final String secretKey;

	public JWTUtil(@Value("${spring.jwt.secret}") String secretKey) {
		this.secretKey = secretKey;
	}

	public String getUsername(String token) {

		Claims claims = Jwts.parser()
			.setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
			.parseClaimsJws(token)
			.getBody();

		return claims.get("username", String.class);

	}

	public String getRole(String token) {

		Claims claims = Jwts.parser()
			.setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
			.parseClaimsJws(token)
			.getBody();

		return claims.get("role", String.class);

	}

	public String getCategory(String token) {

		Claims claims = Jwts.parser()
			.setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
			.parseClaimsJws(token)
			.getBody();

		return claims.get("category", String.class);

	}

	public void isExpired(String token) {

		Claims claims = Jwts.parser()
			.setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
			.parseClaimsJws(token)
			.getBody();

	}

	public String createJwt(String category, String username, String role, Long expiredMs) {

		return Jwts.builder()
			.claim("category", category)
			.claim("username", username)
			.claim("role", role)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + expiredMs))
			.signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
			.compact();

	}

	// JWT 토큰 유효성 검사 메서드
	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}