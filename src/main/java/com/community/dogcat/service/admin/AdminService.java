package com.community.dogcat.service.admin;

import com.community.dogcat.domain.*;
import com.community.dogcat.dto.admin.AdminUserDetailDTO;
import com.community.dogcat.dto.admin.BlockListDTO;
import com.community.dogcat.dto.admin.ReportListDTO;
import com.community.dogcat.dto.board.BoardPageRequestDTO;
import com.community.dogcat.dto.report.ReportDetailDTO;
import com.community.dogcat.repository.admin.AdminRepository;
import com.community.dogcat.repository.report.ReportLogRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final ReportLogRepository reportLogRepository;
    private final AdminRepository adminRepository;
    private final UsersAuthRepository usersAuthRepository;

    public List<AdminUserDetailDTO> findAllUsers(BoardPageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("userId");
        Page<User> userPage;

        // null, 값이 없는유저 제외
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            // keyword로 검색하여 block되지 않은 유저목록 불러옴
            userPage = adminRepository.findByBlockFalseKeyword(keyword, keyword, pageable);
        } else {
            userPage = adminRepository.findByBlockFalse(pageable);
        }

        //userPage담아 반환
        return userPage.getContent().stream()
                .map(user -> {
                    UsersAuth userAuth = usersAuthRepository.findByUserId(user.getUserId());
                    return AdminUserDetailDTO.builder()
                            .userId(user.getUserId())
                            .userName(user.getUserName())
                            .regDate(user.getRegDate())
                            .nickname(user.getNickname())
                            .userVet(user.isUserVet())
                            .authorities(userAuth != null ? userAuth.getAuthorities() : null)//유저 권한 가져옴
                            .build();
                })
                .collect(Collectors.toList());
    }

    public int countAllUsers(BoardPageRequestDTO pageRequestDTO) {
        // 차단당하지 않은 유저 숫자
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            return (int) adminRepository.countByBlockFalseKeyword(keyword, keyword);
        } else {
            return (int) adminRepository.countByBlockFalse();
        }
    }

    public List<ReportListDTO> findAllReportedUsers(BoardPageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("userId");
        Page<ReportLog> reportLogPage;

        // null, 값이 없는유저 제외
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            // 신고 목록 불러옴
            reportLogPage = reportLogRepository.findByReportTitleContaining(keyword, pageable);
        } else {
            reportLogPage = reportLogRepository.findAll(pageable);
        }

        return reportLogPage.getContent().stream()
                .filter(reportLog -> {
                    // Null 체크, 차단된 유저 필터링
                    boolean postNotBlocked = reportLog.getPostNo() != null && !reportLog.getPostNo().getUserId().isBlock();
                    boolean replyNotBlocked = reportLog.getReplyNo() != null && !reportLog.getReplyNo().getUserId().isBlock();
                    return postNotBlocked || replyNotBlocked;
                })
                .map(reportLog -> ReportListDTO.builder()
                        .userId(reportLog.getUserId())
                        .nickname(reportLog.getUserId().getNickname())
                        .reportNo(reportLog.getReportNo())
                        .reportTitle(reportLog.getReportTitle())
                        .reportContent(reportLog.getReportContent())
                        .regDate(reportLog.getRegDate())
                        .post(reportLog.getPostNo())
                        .reply(reportLog.getReplyNo())
                        .build())
                .collect(Collectors.toList());
    }

    public int countAllReportUser(BoardPageRequestDTO pageRequestDTO) {
        // 신고당한 유저 숫자
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            return (int) reportLogRepository.countByReportTitleContaining(keyword);
        } else {
            return (int) reportLogRepository.count();
        }
    }

    public ReportDetailDTO findByReportDetail(Long reportNo) {
        ReportLog reportLog = reportLogRepository.findById(reportNo).orElse(null);

        if (reportLog == null) {
            return null;
        }

        User user = reportLog.getUserId();
        Post post = reportLog.getPostNo();
        Reply reply = reportLog.getReplyNo();

        String userName = user.getUserName();
        String nickname = user.getNickname();
        //reportLog에 post나 reply 값중 하나만 들어있음
        String postTitle = post != null ? post.getPostTitle() : null;
        String replyContent = reply != null ? reply.getReplyContent() : null;

        ReportDetailDTO reportDetailDTO = ReportDetailDTO.builder()
                .userId(user)
                .userName(userName)
                .nickname(nickname)
                .reportNo(reportLog.getReportNo())
                .reportTitle(reportLog.getReportTitle())
                .reportContent(reportLog.getReportContent())
                .regDate(reportLog.getRegDate())
                .postNo(post)
                .postTitle(postTitle)
                .replyNo(reply)
                .replyContent(replyContent)
                .build();

        return reportDetailDTO;
    }

    public List<BlockListDTO> findBlockUsers(BoardPageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("userId");
        Page<User> blockUserPage;

        // null, 값이 없는유저 제외
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            blockUserPage = adminRepository.findByBlockTrueKeyword(keyword, keyword, pageable);
        } else {
            blockUserPage = adminRepository.findByBlockIsTrue(pageable);
        }

        return blockUserPage.getContent().stream()
                .map(user -> BlockListDTO.builder()
                        .userId(user.getUserId())
                        .userName(user.getUserName())
                        .regDate(user.getRegDate())
                        .block(user.isBlock())
                        .nickname(user.getNickname())
                        .userVet(user.isUserVet())
                        .build())
                .collect(Collectors.toList());
    }

    public int countAllBlockUsers(BoardPageRequestDTO pageRequestDTO) {
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            return (int) adminRepository.countByBlockTrueKeyword(keyword, keyword);
        } else {
            return (int) adminRepository.countByBlockTrue();
        }
    }

    //유저 차단
    public void blockUser(String userId) {
        int updatedCount = adminRepository.blockUserByUserId(userId);
        if (updatedCount > 0) {
            log.info("User " + userId + " blocked successfully.");
        } else {
            log.warn("User not found with userId '" + userId + "'.");
        }
    }

    //관리자 지정
    public void designateAdministrator(String userId) {
        UsersAuth usersAuth = UsersAuth.builder().userId(userId).authorities("ROLE_ADMIN").build();

        usersAuthRepository.save(usersAuth);
    }

    //유저 차단 해제
    public void restoreUser(String userId) {
        int updateCount = adminRepository.restoreUserByUserId(userId);
        if (updateCount < 1) {
            log.info("User " + userId + " restored successfully. ");
        } else {
            log.warn("User not found with userId '" + userId + "'.");
        }
    }

}
