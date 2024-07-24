package com.community.dogcat.controller.admin;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.domain.User;
import com.community.dogcat.dto.admin.AdminUserDetailDTO;
import com.community.dogcat.dto.admin.BlockListDTO;
import com.community.dogcat.dto.admin.ReportListDTO;
import com.community.dogcat.dto.board.BoardPageRequestDTO;
import com.community.dogcat.dto.board.BoardPageResponseDTO;
import com.community.dogcat.dto.report.ReportDetailDTO;
import com.community.dogcat.dto.report.UserReportDetailDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.admin.AdminService;
import com.community.dogcat.service.report.ReportService;
import com.community.dogcat.service.user.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@Secured("ROLE_ADMIN")
@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {

    public AdminController(JWTUtil jwtUtil, UserService userService, AdminService adminService,
                           ReportService reportService) {
        super(jwtUtil, userService);
        this.adminService = adminService;
        this.userService = userService;
        this.reportService = reportService;
    }

    private final AdminService adminService;
    private final UserService userService;
    private final ReportService reportService;

    @GetMapping("/user-list")
    public void adminUserDetail(BoardPageRequestDTO pageRequestDTO, Model model) {

        List<AdminUserDetailDTO> adminUsers = adminService.findAllUsers(pageRequestDTO);
        int totalUsers = adminService.countAllUsers(pageRequestDTO);


        BoardPageResponseDTO<AdminUserDetailDTO> pageResponseDTO = BoardPageResponseDTO.<AdminUserDetailDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(adminUsers)
                .total(totalUsers)
                .build();

        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("pageResponse", pageResponseDTO);
        model.addAttribute("searchKeyword", pageRequestDTO.getKeyword());
    }


    @GetMapping("/user-report")
    public void adminUserReport(@RequestParam("userId") String userId, @RequestParam("nickname") String nickname,
                                Model model) {
        User user = userService.findUserId(userId);
        List<UserReportDetailDTO> reportedPosts = reportService.findReportedPostsByUserId(userId);
        List<UserReportDetailDTO> reportedReplys = reportService.findReportedReplysByUserId(userId);


        model.addAttribute("user", user);
        model.addAttribute("reportNickname", nickname);
        model.addAttribute("reportedPosts", reportedPosts);
        model.addAttribute("reportedReplys", reportedReplys);


    }


    @GetMapping("/report-list")
    public void reportList(BoardPageRequestDTO pageRequestDTO, Model model) {

        List<ReportListDTO> reportLists = adminService.findAllReportedUsers(pageRequestDTO);
        int totalReportUsers = adminService.countAllReportUser(pageRequestDTO);

        // PageResponseDTO 생성
        BoardPageResponseDTO<ReportListDTO> pageResponseDTO = BoardPageResponseDTO.<ReportListDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(reportLists)
                .total(totalReportUsers)
                .build();

        // model에 결과를 담아서 view로 전달
        model.addAttribute("reportList", reportLists);
        model.addAttribute("pageResponse", pageResponseDTO);
    }


    @GetMapping("/block-list")
    public void blockList(BoardPageRequestDTO pageRequestDTO, Model model) {

        // 블록 리스트 조회
        List<BlockListDTO> blockLists = adminService.findBlockUsers(pageRequestDTO);
        int totalBlockUsers = adminService.countAllBlockUsers(pageRequestDTO);


        // PageResponseDTO 생성
        BoardPageResponseDTO<BlockListDTO> pageResponseDTO = BoardPageResponseDTO.<BlockListDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(blockLists)
                .total(totalBlockUsers)
                .build();

        // 모델에 결과를 담아서 view로 전달
        model.addAttribute("blockList", blockLists);
        model.addAttribute("pageResponse", pageResponseDTO);
    }


    @GetMapping("/report-detail")
    public void reportDetail(@RequestParam("reportNo") Long reportNo, Model model) {

        ReportDetailDTO reportDetail = adminService.findByReportDetail(reportNo);
        model.addAttribute("reportDetail", reportDetail);

    }

    @PostMapping("/blockUser/{userId}")
    public ResponseEntity<String> blockUser(@PathVariable String userId) {
        adminService.blockUser(userId);
        return ResponseEntity.ok("User " + userId + " blocked successfully.");
    }

    @PostMapping("/administrator/{userId}")
    public ResponseEntity<String> designateAdministrator(@PathVariable String userId) {
        adminService.designateAdministrator(userId);
        return ResponseEntity.ok("User " + userId + " blocked successfully.");
    }

    @PostMapping("/restoreUser/{userId}")
    public ResponseEntity<String> restoreUser(@PathVariable String userId) {
        adminService.restoreUser(userId);
        return ResponseEntity.ok("User" + userId + " restored successfully.");
    }

    @PostMapping("/deleteReportLog/{reportNo}")
    public ResponseEntity<String> deleteReportLog(@PathVariable Long reportNo) {
        adminService.deleteReportLog(reportNo);
        return ResponseEntity.ok("ReportLog " + reportNo + " deleted successfully.");
    }

}


