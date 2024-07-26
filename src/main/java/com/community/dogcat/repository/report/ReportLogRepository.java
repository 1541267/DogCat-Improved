package com.community.dogcat.repository.report;

import com.community.dogcat.domain.Post;
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
import java.util.Optional;

public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {

    ReportLog findByUserId(User userId);

    ReportLog save(ReportLogDTO reportLog);

    @Query("SELECT r.reportNo FROM ReportLog r WHERE r.postNo.postNo = :postNo")
    List<Long> findByPostNo(@Param("postNo") Long postNo);

    @Query("SELECT r.reportNo FROM ReportLog r WHERE r.replyNo.replyNo = :replyNo")
    List<Long> findByReplyNo(@Param("replyNo") Long replyNo);

    // 받은 신고 목록
    Page<ReportLog> findByReportTitleContaining(String keyword, Pageable pageable);

    // 받은 신고 숫자
    @Query("SELECT COUNT(r) FROM ReportLog r WHERE r.reportTitle LIKE %:keyword%")
    long countByReportTitleContaining(@Param("keyword") String keyword);

    // 신고 삭제
    @Modifying
    @Query("DELETE FROM ReportLog rl WHERE rl.reportNo = :reportNo")
    int deleteReportLog(@Param("reportNo") Long reportNo);


}
