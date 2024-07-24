package com.community.dogcat.repository.upload;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;

public interface UploadRepository extends JpaRepository<ImgBoard, String> {

	List<ImgBoard> findByPostNo(Post postNo);
}
