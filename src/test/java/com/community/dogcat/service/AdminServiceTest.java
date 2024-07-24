package com.community.dogcat.service;

import com.community.dogcat.domain.ReportLog;
import com.community.dogcat.domain.User;
import com.community.dogcat.repository.report.ReportLogRepository;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.service.admin.AdminService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log4j2
class AdminServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportLogRepository reportLogRepository;

    @Autowired
    private AdminService adminService;

    @Test
    public void getUserAndReportLog() {

        User user = userRepository.findByUserId("test");
        log.info(user.getUserId());

        log.info("-------------" + user);

        ReportLog reportLog = reportLogRepository.findByUserId(user);
        log.info(reportLog.getReportNo());

    }

//	@Test
//	void findAllReportedUsers() {
//		log.info("findallreportuser");
//		adminService.findAllReportedUsers();
//	}

}
