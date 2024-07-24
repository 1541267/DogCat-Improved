package com.community.dogcat.service;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.domain.ReportLog;
import com.community.dogcat.domain.User;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.report.ReportLogRepository;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.service.admin.AdminService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Optional;

@SpringBootTest
@Log4j2
class ReportServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportLogRepository reportLogRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private BoardRepository boardRepository;


    @Test
    public void createReportLog() {

        User user = userRepository.findByUserId("test");
        Post post = boardRepository.findById(1L).orElse(null);

        ReportLog reportLog = ReportLog.builder()
                .userId(user)  // Store user ID as String
                .postNo(post)
                .reportTitle("테스트로 만든 리폿 제목")
                .reportContent("테스트 리폿 내용")
                .regDate(Instant.now())
                .build();

        reportLogRepository.save(reportLog);
    }

    @Test
    public void report() {
        Post postNo = Post.builder()
                .postNo(1L)
                .postContent("테스트 아아아")
                .build();

        Reply replyNo = Reply.builder().replyNo(1L).build();

        Optional<Post> a = boardRepository.findById(2L);

        log.info("Report GetPostNo: {}, GetPostContent: {}", postNo.getPostNo(), postNo.getPostContent());

        log.info("Optional PostNo: {}, PostTitle: {}", a.get().getPostNo(), a.get().getPostTitle());
    }


}
