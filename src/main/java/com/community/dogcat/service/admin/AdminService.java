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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final ReportLogRepository reportLogRepository;
    private final AdminRepository adminRepository;
    private final UsersAuthRepository usersAuthRepository;

    public List<AdminUserDetailDTO> findAllUsers(BoardPageRequestDTO pageRequestDTO, String viewStyle) {

        Pageable pageable = pageRequestDTO.getPageable("userId");
        Page<User> userPage;

        // null, 값이 없는 유저 제외
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            // keyword로 검색하여 block되지 않은 유저목록 불러옴
            userPage = adminRepository.findByBlockFalseKeyword(keyword, keyword, pageable);
        } else {
            userPage = adminRepository.findByBlockFalse(pageable);
        }

        // 사용자 목록을 가져와 AdminUserDetailDTO 리스트로 변환
        List<AdminUserDetailDTO> users = userPage.getContent().stream()
                .map(user -> {
                    UsersAuth userAuth = usersAuthRepository.findByUserId(user.getUserId());
                    return AdminUserDetailDTO.builder()
                            .userId(user.getUserId())
                            .userName(user.getUserName())
                            .regDate(user.getRegDate())
                            .nickname(user.getNickname())
                            .userVet(user.isUserVet())
                            .authorities(userAuth.getAuthorities()) // 유저 권한 가져옴
                            .build();
                })
                .collect(Collectors.toList());

        // 권한이 "ROLE_ADMIN"인 사용자와 그렇지 않은 사용자로 분리
        List<AdminUserDetailDTO> adminUsers = users.stream()
                .filter(user -> user.getAuthorities().contains("ROLE_ADMIN"))
                .collect(Collectors.toList());
        List<AdminUserDetailDTO> nonAdminUsers = users.stream()
                .filter(user -> !user.getAuthorities().contains("ROLE_ADMIN"))
                .collect(Collectors.toList());

        // viewStyle 관리자 우선보기일 경우
        if ("adminFirst".equalsIgnoreCase(viewStyle)) {
            adminUsers.addAll(nonAdminUsers);
            return adminUsers;
        } else {
            // 아닐 경우 유저 우선보기
            nonAdminUsers.addAll(adminUsers);
            return nonAdminUsers;
        }
    }


    public int countAllUsers(BoardPageRequestDTO pageRequestDTO) {
        // 차단당하지 않은 유저 인원수
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
            reportLogPage = reportLogRepository.findByReportNotBlockedKeyword(keyword, pageable);
        } else {
            reportLogPage = reportLogRepository.findByReportNotBlocked(pageable);
        }

        return reportLogPage.getContent().stream()
                .map(reportLog -> ReportListDTO.builder()
                        .user(reportLog.getUserId()) //신고한 유저
                        .nickname(reportLog.getUserId().getNickname())

                        .reportedUser(reportLog.getPostNo() != null ? reportLog.getPostNo().getUserId() : reportLog.getReplyNo().getUserId())// 신고 받은 유저
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
        // 신고당한 유저 인원수
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            String keyword = pageRequestDTO.getKeyword();
            return (int) reportLogRepository.countByReportNotBlockedKeyword(keyword);
        } else {
            return (int) reportLogRepository.countByReportNotBlocked();
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
        // 차단 당한 유저 인원수
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
        if (updatedCount < 1) {
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
        if (updateCount > 0){
            log.warn("User not found with userId '" + userId + "'.");
        }
    }

}
