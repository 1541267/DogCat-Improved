package com.community.dogcat.service.admin;

import com.community.dogcat.domain.*;
import com.community.dogcat.dto.admin.AdminUserDetailDTO;
import com.community.dogcat.dto.admin.BlockListDTO;
import com.community.dogcat.dto.admin.ReportListDTO;
import com.community.dogcat.dto.board.BoardPageRequestDTO;
import com.community.dogcat.dto.report.ReportDetailDTO;
import com.community.dogcat.repository.admin.AdminRepository;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.report.ReportLogRepository;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;
import com.community.dogcat.service.user.UserService;
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
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final UsersAuthRepository usersAuthRepository;
    private final BoardRepository boardRepository;
    private final UserService userService;

    public List<AdminUserDetailDTO> findAllUsers(BoardPageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("userId");
        Page<User> userPage;

        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            userPage = adminRepository.findAllByBlockFalseAndNicknameContainingOrBlockFalseAndUserNameContaining(keyword, keyword, pageable);
        } else {
            userPage = adminRepository.findByBlockIsFalse(pageable);
        }

        return userPage.getContent().stream()
                .map(user -> AdminUserDetailDTO.builder()
                        .userId(user.getUserId())
                        .userName(user.getUserName())
                        .regDate(user.getRegDate())
                        .nickname(user.getNickname())
                        .build())
                .collect(Collectors.toList());
    }

    public int countAllUsers(BoardPageRequestDTO pageRequestDTO) {
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            return (int) adminRepository.countByBlockFalseAndNicknameContainingOrUserNameContaining(keyword, keyword);
        } else {
            return (int) adminRepository.countByBlockFalse();
        }
    }


    public List<ReportListDTO> findAllReportedUsers(BoardPageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("userId");
        Page<ReportLog> reportLogPage;

        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            reportLogPage = reportLogRepository.findAllByReportTitleContaining(keyword, pageable);
        } else {
            reportLogPage = reportLogRepository.findAll(pageable);
        }

        return reportLogPage.getContent().stream()
                .filter(reportLog -> !reportLog.getUserId().isBlock())
                .map(reportLog -> ReportListDTO.builder()
                        .userId(reportLog.getUserId())
                        .nickname(reportLog.getUserId().getNickname())
                        .reportNo(reportLog.getReportNo())
                        .reportTitle(reportLog.getReportTitle())
                        .reportContent(reportLog.getReportContent())
                        .regDate(reportLog.getRegDate())
                        .postNo(reportLog.getPostNo())
                        .replyNo(reportLog.getReplyNo())
                        .build())
                .collect(Collectors.toList());
    }

    public int countAllReportUser(BoardPageRequestDTO pageRequestDTO) {
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            return (int) reportLogRepository.countByReportTitleContaining(keyword);
        } else {
            return (int) reportLogRepository.count();
        }
    }


    public ReportDetailDTO findByReportDetail(Long reportNo) {
        ReportLog reportDetail = reportLogRepository.findById(reportNo).orElse(null);

        if (reportDetail == null) {
            return null;
        }

        User user = reportDetail.getUserId();
        Post post = reportDetail.getPostNo();
        Reply reply = reportDetail.getReplyNo();

        String userName = user.getUserName();
        String nickname = user.getNickname();
        String postTitle = post != null ? post.getPostTitle() : null;
        String replyContent = reply != null ? reply.getReplyContent() : null;

        ReportDetailDTO reportDetailDTO = ReportDetailDTO.builder()
                .userId(user)
                .userName(userName)
                .nickname(nickname)
                .reportNo(reportDetail.getReportNo())
                .reportTitle(reportDetail.getReportTitle())
                .reportContent(reportDetail.getReportContent())
                .regDate(reportDetail.getRegDate())
                .postNo(post)
                .postTitle(postTitle)
                .replyNo(reply)
                .replyContent(replyContent)
                .build();

        return reportDetailDTO;
    }

    // public List<BlockListDTO> findBlockUsers() {
    public List<BlockListDTO> findBlockUsers(BoardPageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("userId"); // 기본 정렬 필드는 userId로 예시
        Page<User> blockUserPage;

        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            blockUserPage = adminRepository.findAllByBlockTrueAndNicknameContainingOrBlockTrueAndUserNameContaining(keyword, keyword, pageable);
        } else {
            blockUserPage = adminRepository.findByBlockIsTrue(pageable);
        }

        return blockUserPage.getContent().stream()
                .map(user -> BlockListDTO.builder()
                        .userId(user.getUserId())
                        .userName(user.getUserName())
                        .regDate(user.getRegDate())
                        .block(user.isBlock())
                        .build())
                .collect(Collectors.toList());
    }


    public int countAllBlockUsers(BoardPageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            return (int) adminRepository.countByBlockTrueAndNicknameContainingOrUserNameContaining(keyword, keyword);
        } else {
            return (int) adminRepository.countByBlockTrue();
        }

    }


    public void blockUser(String userId) {
        int updatedCount = adminRepository.blockUserByUserId(userId);
        if (updatedCount > 0) {
            log.info("User " + userId + " blocked successfully.");
        } else {
            log.warn("User not found with userId '" + userId + "'.");
        }
    }

    public void designateAdministrator(String userId) {
        UsersAuth usersAuth = UsersAuth.builder().userId(userId).authorities("ROLE_ADMIN").build();

        usersAuthRepository.save(usersAuth);
    }

    public void restoreUser(String userId) {
        int updateCount = adminRepository.restoreUserByUserId(userId);
        if (updateCount < 1) {
            log.info("User " + userId + " restored successfully. ");
        } else {
            log.warn("User not found with userId '" + userId + "'.");
        }
    }


    @Transactional
    public void deleteReportLog(Long reportNo) {
        adminRepository.deleteReportLog(reportNo);
    }

}
