package com.community.dogcat.repository.admin;

import com.community.dogcat.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AdminRepository extends JpaRepository<User, String> {


    // 유저 차단
    @Modifying
    @Query("UPDATE User u SET u.block = true WHERE u.userId = :userId")
    int blockUserByUserId(@Param("userId") String userId);

    // 차단 유저 복구
    @Modifying
    @Query("UPDATE User u SET u.block = false WHERE u.userId = :userId")
    int restoreUserByUserId(@Param("userId") String userId);

    // 차단당하지 않은 유저 목록
    @Query("SELECT u FROM User u WHERE u.block = false AND u.nickname LIKE %:keyword1% AND u.block = false AND u.userName LIKE %:keyword2%")
    Page<User> findByBlockFalseKeyword(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2, Pageable pageable);
    Page<User> findByBlockFalse(Pageable pageable);

    // 차단당하지 않은 유저 숫자
    @Query("SELECT COUNT(u) FROM User u WHERE u.block = false AND (u.nickname LIKE %:keyword1% OR u.userName LIKE %:keyword2%)")
    long countByBlockFalseKeyword(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2);
    long countByBlockFalse();

    // 차단당한 유저 목록
    @Query("SELECT u FROM User u WHERE u.block = true AND (u.nickname LIKE %:keyword1% OR u.userName LIKE %:keyword2%)")
    Page<User> findByBlockTrueKeyword(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2, Pageable pageable);
    Page<User> findByBlockIsTrue(Pageable pageable);

    // 차단당한 유저 숫자
    @Query("SELECT COUNT(u) FROM User u WHERE u.block = true AND (u.nickname LIKE %:keyword1% OR u.userName LIKE %:keyword2%)")
    long countByBlockTrueKeyword(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2);
    long countByBlockTrue();


}