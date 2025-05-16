package com.community.dogcat.repository.board;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.User;
import com.community.dogcat.repository.search.BoardSearch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Post, Long>, BoardSearch {

	// 상세페이지 접속시 조회수 증가
	@Modifying
	@Query("UPDATE Post p SET p.viewCount = p.viewCount+1 WHERE p.postNo = :postNo")
	void updateViewCount(@Param("postNo") Long postNo);

	// userId 와 postNo 가 일치하는 게시물 찾기
	@Query("SELECT p FROM Post p WHERE p.postNo = :postNo AND p.userId = :userId")
	Optional<Post> findByPostNoAndUserId(@Param("postNo") Long postNo, @Param("userId") User userId);

	// UserI에 해당하는 게시물 리스트에 담기
	@Query("SELECT p FROM Post p WHERE p.userId = :userId")
	List<Post> findByUserId(User userId);

	// userId에 해당하는 게시글 갯수 카운트(마이페이지용)
	@Query("SELECT count(p) FROM Post p WHERE p.userId.userId = :userId")
	Long countPostsByUser(@Param("userId") String userId);

	// S3 업로드시 게시글 본문의 이미지 링크 변환
	// @Modifying
	// @Transactional
	// @Query("UPDATE Post p SET p.postContent = REPLACE(p.postContent, :oldUrl, :newUrl) WHERE p.postNo = :postNo")
	// void updatePostByS3img(@Param("oldUrl") String oldUrl, @Param("newUrl") String newUrl,
	// 	@Param("postNo") Long postNo);

	Post findByPostNo(Long postNo);

	@Transactional
	void deleteAllByUserId(User user);

	List<Post> findAllByUserId(User user);

	// 더미데이터 제거용 / 25-05-16
	@Transactional
	@Modifying
	@Query("DELETE FROM Post p WHERE p.postNo >= :threshold")
	void deleteAllByPostNoMore1000(@Param("threshold") Long threshold);


}
