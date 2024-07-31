package com.community.dogcat.service.report;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.domain.ReportLog;
import com.community.dogcat.domain.User;
import com.community.dogcat.dto.report.ReportLogDTO;
import com.community.dogcat.dto.report.UserReportDetailDTO;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.board.reply.ReplyRepository;
import com.community.dogcat.repository.report.ReportLogRepository;
import com.community.dogcat.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportLogRepository reportLogRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;

    public void createReportLog(ReportLogDTO reportLogDTO) {

        //신고 생성
        ReportLog reportLog = ReportLog.builder()
                .userId(userRepository.findByUserId(reportLogDTO.getUserId()))
                .postNo(boardRepository.findByPostNo(reportLogDTO.getPostNo()))
                .replyNo(replyRepository.findByReplyNo(reportLogDTO.getReplyNo()))
                .reportTitle(reportLogDTO.getReportTitle())
                .reportContent(reportLogDTO.getReportContent())
                .regDate(Instant.now())
                .build();

        reportLogRepository.save(reportLog);
    }

    public List<UserReportDetailDTO> findReportedByUserId(String userId) {
        User user = userRepository.findByUserId(userId);
        List<UserReportDetailDTO> userReportDetailDTOS = new ArrayList<>();

        // 게시글 신고 정보 가져오기
        List<Post> userPosts = boardRepository.findByUserId(user);
        for (Post post : userPosts) {
            List<Long> reportNos = reportLogRepository.findByPostNo(post.getPostNo());
            for (Long reportNo : reportNos) {
                UserReportDetailDTO dto = UserReportDetailDTO.builder()
                        .postNo(post.getPostNo())
                        .postTitle(post.getPostTitle())
                        .reportNo(reportNo)
                        .regDate(post.getRegDate())
                        .boardCode(post.getBoardCode())
                        .build();
                userReportDetailDTOS.add(dto);
            }
        }

        // 댓글 신고 정보 가져오기
        List<Reply> userReplies = replyRepository.findByUserId(user);
        for (Reply reply : userReplies) {
            List<Long> reportNos = reportLogRepository.findByReplyNo(reply.getReplyNo());
            for (Long reportNo : reportNos) {
                UserReportDetailDTO dto = UserReportDetailDTO.builder()
                        .replyNo(reply.getReplyNo())
                        .replyContent(reply.getReplyContent())
                        .reportNo(reportNo)
                        .ReplyregDate(reply.getRegDate())
                        .build();
                userReportDetailDTOS.add(dto);
            }
        }
        // reportNo 순으로 정렬
        userReportDetailDTOS.sort(Comparator.comparing(UserReportDetailDTO::getReportNo));

        return userReportDetailDTOS;
    }


    //신고 삭제
    @Transactional
    public void deleteReportLog(Long reportNo) {
        reportLogRepository.deleteReportLog(reportNo);
    }



}
