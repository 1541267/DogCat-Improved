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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    public void adminUserDetail(BoardPageRequestDTO pageRequestDTO, Model model, @RequestParam(value = "viewStyle", defaultValue = "nonAdminFirst") String viewStyle) {

        System.out.println("viewStyle : " + viewStyle);

        //userId로 유저 목록 불러옴
        List<AdminUserDetailDTO> adminUsers = adminService.findAllUsers(pageRequestDTO, viewStyle);
        int totalUsers = adminService.countAllUsers(pageRequestDTO);

        BoardPageResponseDTO<AdminUserDetailDTO> pageResponseDTO = BoardPageResponseDTO.<AdminUserDetailDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(adminUsers)
                .total(totalUsers)
                .build();

        // model에 결과를 담아서 view로 전달
        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("pageResponse", pageResponseDTO);
        model.addAttribute("searchKeyword", pageRequestDTO.getKeyword());

    }



    @GetMapping("/user-report")
    public void adminUserReport(@RequestParam("userId") String userId, @RequestParam("nickname") String nickname,
                                Model model) {
        User user = userService.findUserId(userId);

        // 신고 정보 가져옴
        List<UserReportDetailDTO> reportedDetails = reportService.findReportedByUserId(userId);

        // model에 결과를 담아서 view로 전달
        model.addAttribute("user", user);
        model.addAttribute("reportNickname", nickname);
        model.addAttribute("reportedDetails", reportedDetails);
    }


    @GetMapping("/report-list")
    public void reportList(BoardPageRequestDTO pageRequestDTO, Model model) {

        // 받은 신고 조회
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
        model.addAttribute("searchKeyword", pageRequestDTO.getKeyword());

    }


    @GetMapping("/block-list")
    public void blockList(BoardPageRequestDTO pageRequestDTO, Model model) {

        // 차단 회원 조회
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
        model.addAttribute("searchKeyword", pageRequestDTO.getKeyword());

    }


    @GetMapping("/report-detail")
    public void reportDetail(@RequestParam("reportNo") Long reportNo, Model model) {

        //받은 신고 상세내역
        ReportDetailDTO reportDetail = adminService.findByReportDetail(reportNo);
        model.addAttribute("reportDetail", reportDetail);

    }

    @PostMapping("/blockUser/{userId}")
    public ResponseEntity<String> blockUser(@PathVariable String userId) {

        //회원 차단 처리
        adminService.blockUser(userId);
        return ResponseEntity.ok("User " + userId + " blocked successfully.");
    }

    @PostMapping("/administrator/{userId}")
    public ResponseEntity<String> designateAdministrator(@PathVariable String userId) {

        //관리자 권한 부여
        adminService.designateAdministrator(userId);
        return ResponseEntity.ok("User " + userId + " blocked successfully.");
    }

    @PostMapping("/restoreUser/{userId}")
    public ResponseEntity<String> restoreUser(@PathVariable String userId) {

        //차단당한 회원 복구
        adminService.restoreUser(userId);
        return ResponseEntity.ok("User" + userId + " restored successfully.");
    }

    @PostMapping("/deleteReportLog/{reportNo}")
    public ResponseEntity<String> deleteReportLog(@PathVariable Long reportNo) {

        //받은 신고 삭제처리
        reportService.deleteReportLog(reportNo);
        return ResponseEntity.ok("ReportLog " + reportNo + " deleted successfully.");
    }

}


