package com.community.dogcat.repository.board.scrap;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Scrap;
import com.community.dogcat.domain.User;
import com.community.dogcat.repository.search.ScrapSearch;

public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapSearch {

	// postNo와 userId로 해당하는 Scrap가 있는지 확인
	@Query("SELECT s FROM Scrap s WHERE s.postNo = :postNo AND s.userId = :userId")
	Optional<Scrap> findByPostNoAndUserId(@Param("postNo") Post postNo, @Param("userId") User userId);

	// userId에 해당하는 Scrap을 찾아서 목록에 담기
	@Query("SELECT s FROM Scrap s WHERE s.userId = :userId")
	List<Scrap> findByUserId(@Param("userId") User userId);

	// userId에 해당하는 댓글 갯수 카운트(마이페이지용)
	@Query("SELECT count(s) FROM Scrap s WHERE s.userId.userId = :userId")
	Long countScrapsByUser(@Param("userId") String userId);
}
