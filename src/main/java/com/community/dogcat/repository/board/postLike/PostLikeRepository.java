package com.community.dogcat.repository.board.postLike;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.PostLike;
import com.community.dogcat.domain.User;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	//postNo와 userId로 해당하는 postLike가 있는지 확인
	@Query("select pl from PostLike pl where pl.postNo = :postNo and pl.userId = :userId")
	Optional<PostLike> findByPostAndUser(@Param("postNo") Post postNo, @Param("userId") User userId);

	@Transactional
	void deleteAllByUserId(User user);

	List<PostLike> findAllByUserId(User user);
}
