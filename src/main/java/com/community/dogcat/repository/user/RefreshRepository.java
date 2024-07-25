package com.community.dogcat.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.RefreshEntity;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

	Boolean existsByRefresh(String refresh);

	@Transactional
	void deleteByRefresh(String refresh);

	@Transactional
	void deleteAllByUsername(String userName);

}
