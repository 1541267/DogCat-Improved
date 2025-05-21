package com.community.dogcat.repository.upload;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.dto.uploadImage.FileInfoDTO;

public interface UploadRepository extends JpaRepository<ImgBoard, String> {

	@Query("SELECT I FROM ImgBoard I WHERE I.postNo.postNo = :postNo")
	List<ImgBoard> findByPostNo(Long postNo);

	void deleteByUploadPath(String uploadPath);

	// 개선, 더미데이터 제거용 / 25-05-16
	@Transactional
	@Modifying
	@Query("DELETE FROM ImgBoard i WHERE i.postNo.postNo >= :threshold")
	void deleteAllByPostNoMore1000(@Param("threshold") Long threshold);

	// 개선, 파일 삭제 가능 마크가 된 데이터를 전부 불러옴
	@Query("SELECT new com.community.dogcat.dto.uploadImage.FileInfoDTO"
		+ "(i.fileUuid, i.extension, i.uploadTime, i.deletePossible) "
		+ "FROM ImgBoard i"
		+ " WHERE i.deletePossible = true")
	Set<FileInfoDTO> findFileUuidAndExtensionAndUploadTimeAndDeletePossibleByDeletePossibleTrue();

	@Query("SELECT new com.community.dogcat.dto.uploadImage.FileInfoDTO"
		+ "(i.fileUuid, i.extension, i.uploadTime, i.deletePossible) "
		+ "FROM ImgBoard i"
		+ " WHERE i.deletePossible = false")
	Set<FileInfoDTO> findFileUuidAndExtensionAndUploadTimeAndDeletePossibleByDeletePossibleFalse();

	@Query("SELECT new com.community.dogcat.dto.uploadImage.FileInfoDTO"
		+ "(i.fileUuid, i.extension, i.uploadTime, i.deletePossible) "
		+ "FROM ImgBoard i WHERE i.postNo.postNo = :postNo")
	Set<FileInfoDTO> findFileInfoByPostNo(Long postNo);

	@Query("SELECT new com.community.dogcat.dto.uploadImage.FileInfoDTO"
		+ "(i.fileUuid, i.extension, i.uploadTime, i.deletePossible) "
		+ "FROM ImgBoard i")
	Set<FileInfoDTO> findAllFileUuidAndExtensionAndUploadTimeAndDeletePossible();

	/** 개선, 게시글 수정 시 없어진 이미지들 mark */
	@Query("UPDATE ImgBoard i SET i.deletePossible = true WHERE i.fileUuid IN (:uuids)")
	@Modifying
	void updateAllDeletePossibleTrueByFileUuid(@Param("uuids") List<String> uuids);

	/** 개선, 게시글 삭제 시 없어진 이미지들 mark */
	@Query("UPDATE ImgBoard i SET i.deletePossible = true, i.postNo = NULL WHERE i.postNo.postNo = :postNo")
	@Modifying
	void markFilesDeletedAndUnlinkPost(Long postNo);

	// 개선 db에 없는 파일 제거하기 위해 fullName 반환
	@Query("SELECT i.fileUuid || i.extension FROM ImgBoard i")
	Set<String> findAllFullName();
}
