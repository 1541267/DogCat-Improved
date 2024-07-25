package com.community.dogcat.repository.user;

import com.community.dogcat.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Boolean existsByUserId(String userId);

    User findByUserId(String userId);

    Optional<User> findByUserNameAndTel(String name, String tel);

    Optional<User> findByUserNameAndUserId(String name, String userId);

    Boolean existsByNickname(String nickname);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.userPw = :password, u.modDate = :modDate WHERE u.userName = :userName AND u.userId = :userId")
    void updatePassword(@Param("userName") String userName, @Param("userId") String userId,
                        @Param("password") String password, @Param("modDate") Instant modDate);

    @Query("SELECT u.userPw FROM User u WHERE u.userId = ?1")
    String findPasswordHashByUsername(String userId);

}
