package com.community.dogcat.repository.report;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.domain.ReportLog;
import com.community.dogcat.domain.User;
import com.community.dogcat.dto.report.ReportLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {

    ReportLog findByUserId(User userId);

    ReportLog save(ReportLogDTO reportLog);

    @Query("SELECT r.reportNo FROM ReportLog r WHERE r.postNo.postNo = :postNo")
    List<Long> findByPostNo(@Param("postNo") Long postNo);

    @Query("SELECT r.reportNo FROM ReportLog r WHERE r.replyNo.replyNo = :replyNo")
    List<Long> findByReplyNo(@Param("replyNo") Long replyNo);


    // 신고 받았으면서 차단 당하지 않은 사람들 목록
    @Query("SELECT r FROM ReportLog r " +
            "LEFT JOIN r.postNo.userId pu " +
            "LEFT JOIN r.replyNo.userId rpu " +
            "WHERE (pu.block = false OR rpu.block = false) " +
            "AND (r.reportContent LIKE %:keyword% OR r.reportTitle LIKE %:keyword%)")
    Page<ReportLog> findByReportNotBlockedKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM ReportLog r " +
            "LEFT JOIN r.postNo.userId pu " +
            "LEFT JOIN r.replyNo.userId rpu " +
            "WHERE pu.block = false OR rpu.block = false")
    Page<ReportLog> findByReportNotBlocked(Pageable pageable);


    // 신고 받았으면서 차단당하지 않은 인원수
    @Query("SELECT COUNT(r) FROM ReportLog r " +
            "LEFT JOIN r.postNo.userId pu " +
            "LEFT JOIN r.replyNo.userId rpu " +
            "WHERE (pu.block = false OR rpu.block = false) " +
            "AND (r.reportContent LIKE %:keyword% OR r.reportTitle LIKE %:keyword%)")
    long countByReportNotBlockedKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(r) FROM ReportLog r " +
            "LEFT JOIN r.postNo.userId pu " +
            "LEFT JOIN r.replyNo.userId rpu " +
            "WHERE pu.block = false OR rpu.block = false")
    long countByReportNotBlocked();


    // 신고 삭제
    @Modifying
    @Query("DELETE FROM ReportLog rl WHERE rl.reportNo = :reportNo")
    int deleteReportLog(@Param("reportNo") Long reportNo);

    void deleteByPostNo(Post post);
    void deleteByReplyNo(Reply reply);

}
