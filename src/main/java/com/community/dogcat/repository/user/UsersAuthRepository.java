package com.community.dogcat.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.community.dogcat.domain.UsersAuth;

public interface UsersAuthRepository extends JpaRepository<UsersAuth, String> {

	UsersAuth findByUserId(String userId);

}
