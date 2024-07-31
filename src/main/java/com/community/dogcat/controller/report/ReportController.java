package com.community.dogcat.controller.report;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.dto.report.ReportLogDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.board.BoardServiceImpl;
import com.community.dogcat.service.board.reply.ReplyServiceImpl;
import com.community.dogcat.service.report.ReportService;
import com.community.dogcat.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/report")
public class ReportController extends BaseController {

    private final BoardServiceImpl boardServiceImpl;
    private final ReplyServiceImpl replyServiceImpl;

    public ReportController(JWTUtil jwtUtil, UserService userService, ReportService reportService, BoardServiceImpl boardServiceImpl, ReplyServiceImpl replyServiceImpl) {
        super(jwtUtil, userService);
        this.reportService = reportService;
        this.boardServiceImpl = boardServiceImpl;
        this.replyServiceImpl = replyServiceImpl;
    }

    private final ReportService reportService;


    @PostMapping("/reportLog")
    public ResponseEntity<String> createReportLog(@RequestBody ReportLogDTO reportLogDTO) {
        //신고 저장
        reportService.createReportLog(reportLogDTO);
        return ResponseEntity.ok("Report log created successfully.");
    }

    @GetMapping("/post-report/{postNo}")
    public String postReport(@PathVariable Long postNo, Model model) {

        //게시글 신고
        Post postReport = boardServiceImpl.findPostByPostNo(postNo);
        model.addAttribute("postReport", postReport);
        return "report/post-report";
    }

    @GetMapping("/reply-report/{replyNo}")
    public String replyReport(@PathVariable Long replyNo, Model model) {

        //댓글 신고
        Reply replyReport = replyServiceImpl.findReplyByReplyNo(replyNo);
        model.addAttribute("replyReport", replyReport);
        return "report/reply-report";
    }


}


