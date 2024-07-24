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
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Log4j2
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
        log.info(reportLogDTO);
        reportService.createReportLog(reportLogDTO);
        return ResponseEntity.ok("Report log created successfully.");

    }

    @GetMapping("/post-report/{postNo}")
    public String postReport(@PathVariable Long postNo, Model model) {
        log.info("Report postNo: {}", postNo);

        Post postReport = boardServiceImpl.findPostByPostNo(postNo);
        log.info(postReport);

        model.addAttribute("postReport", postReport);
        return "report/post-report";
    }

    @GetMapping("/reply-report/{replyNo}")
    public String replyReport(@PathVariable Long replyNo, Model model) {
        log.info("Report replyNo: {}", replyNo);

        Reply replyReport = replyServiceImpl.findReplyByReplyNo(replyNo);
        log.info(replyReport);

        model.addAttribute("replyReport", replyReport);
        return "report/reply-report";
    }


}


