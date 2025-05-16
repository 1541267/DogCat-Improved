package com.community.dogcat.repository.upload;

import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;

public interface UploadRepository extends JpaRepository<ImgBoard, String> {

	@Query("SELECT I FROM ImgBoard I WHERE I.postNo.postNo = :postNo")
	List<ImgBoard> findByPostNo(Long postNo);

	void deleteByUploadPath(String uploadPath);

	// 더미데이터 제거용 / 25-05-16
	@Transactional
	@Modifying
	@Query("DELETE FROM ImgBoard i WHERE i.postNo.postNo >= :threshold")
	void deleteAllByPostNoMore1000(@Param("threshold") Long threshold);

}
