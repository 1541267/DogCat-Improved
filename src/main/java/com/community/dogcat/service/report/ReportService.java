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
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportLogRepository reportLogRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;

    public void createReportLog(ReportLogDTO reportLogDTO) {

        //        Optional<Reply> reply = replyRepository.findById(reportLogDTO.getReplyNo());

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

    public List<UserReportDetailDTO> findReportedPostsByUserId(String userId) {

        User user = userRepository.findByUserId(userId);
        List<Post> userPost = boardRepository.findByUserId(user);
        //userId로 검색한 Post userPost 리스트
        log.info("-----------userPost {}", userPost);

        List<Long> userPostNos = userPost.stream()
                .map(Post::getPostNo)
                .collect(Collectors.toList());
        log.info("-----------userPostNos {}", userPostNos);
        //userPost 에서 postNo만 뽑아서 리스트로 만든 것

        List<UserReportDetailDTO> userReportDetailDTOS = new ArrayList<>();

        for (Post post : userPost) {
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
        return userReportDetailDTOS;


    }

    public List<UserReportDetailDTO> findReportedReplysByUserId(String userId) {

        User user = userRepository.findByUserId(userId);
        List<Reply> userReply = replyRepository.findByUserId(user);
        //userId로 검색한 Reply userReply 리스트
        log.info("-----------userReply {}", userReply);

        List<Long> userReplyNos = userReply.stream()
                .map(Reply::getReplyNo)
                .collect(Collectors.toList());
        log.info("-----------userReplyNos {}", userReplyNos);
        //userPost 에서 postNo만 뽑아서 리스트로 만든 것

        List<UserReportDetailDTO> userReportDetailDTOS = new ArrayList<>();

        for (Reply reply : userReply) {
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
        return userReportDetailDTOS;


    }
    //신고 삭제
    @Transactional
    public void deleteReportLog(Long reportNo) {
        reportLogRepository.deleteReportLog(reportNo);
    }



}
