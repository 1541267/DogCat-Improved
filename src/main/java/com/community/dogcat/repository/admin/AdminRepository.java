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


    @Modifying
    @Query("UPDATE User u SET u.block = true WHERE u.userId = :userId")
    int blockUserByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE User u SET u.block = false WHERE u.userId = :userId")
    int restoreUserByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM ReportLog rl WHERE rl.reportNo = :reportNo")
    int deleteReportLog(@Param("reportNo") Long reportNo);


    // 전체 유저목록
    Page<User> findByBlockFalseAndNicknameContainingOrBlockFalseAndUserNameContaining(String keyword1, String keyword2, Pageable pageable);

    Page<User> findByBlockFalse(Pageable pageable);

    long countByBlockFalseAndNicknameContainingOrUserNameContaining(String keyword1, String keyword2);

    long countByBlockFalse();


    //차단된 유저목록
    Page<User> findByBlockTrueAndNicknameContainingOrBlockTrueAndUserNameContaining(String keyword1, String keyword2, Pageable pageable);

    Page<User> findByBlockIsTrue(Pageable pageable);

    long countByBlockTrueAndNicknameContainingOrUserNameContaining(String keyword1, String keyword2);

    long countByBlockTrue();


}