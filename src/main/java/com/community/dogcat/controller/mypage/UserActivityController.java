package com.community.dogcat.controller.mypage;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.dto.myPage.activity.UserPageRequestDTO;
import com.community.dogcat.dto.myPage.activity.UserPageResponseDTO;
import com.community.dogcat.dto.myPage.activity.UserPostsActivityDTO;
import com.community.dogcat.dto.myPage.activity.UserRepliesActivityDTO;
import com.community.dogcat.dto.myPage.activity.UserScrapsActivityDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.myPage.activity.UserActivityService;
import com.community.dogcat.service.user.UserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@Transactional
@RequestMapping("/my")
public class UserActivityController extends BaseController {

	private final UserActivityService userActivityService;

	@Autowired
	public UserActivityController(JWTUtil jwtUtil, UserService userService, UserActivityService userActivityService) {
		super(jwtUtil, userService);
		this.userActivityService = userActivityService;
	}

	@GetMapping("/userActivity/{userId}/posts")
	public String listWithPosts(@PathVariable String userId, UserPageRequestDTO pageRequestDTO, Model model) {
		log.info("-----------------------userId" + userId);

		UserPageResponseDTO<UserPostsActivityDTO> responseDTO = userActivityService.listWithPosts(pageRequestDTO);
		log.info("Page Request DTO: {}", pageRequestDTO);
		log.info("Response DTO: {}", responseDTO);

		model.addAttribute("pageRequestDTO", pageRequestDTO);
		model.addAttribute("responseDTO", responseDTO);

		return "my/userPostsActivity";
	}

	@GetMapping("/userActivity/{userId}/replies")
	public String listWithReplies(@PathVariable String userId, UserPageRequestDTO pageRequestDTO, Model model) {
		log.info("-----------------------userId" + userId);

		UserPageResponseDTO<UserRepliesActivityDTO> responseDTO = userActivityService.listWithReplies(pageRequestDTO);
		log.info("Page Request DTO: {}", pageRequestDTO);
		log.info("Response DTO: {}", responseDTO);

		model.addAttribute("pageRequestDTO", pageRequestDTO);
		model.addAttribute("responseDTO", responseDTO);

		return "my/userRepliesActivity";
	}

	@GetMapping("/userActivity/{userId}/scraps")
	public String listWithScraps(@PathVariable String userId, UserPageRequestDTO pageRequestDTO, Model model) {
		log.info("-----------------------userId" + userId);

		UserPageResponseDTO<UserScrapsActivityDTO> responseDTO = userActivityService.listWithScraps(pageRequestDTO);
		log.info("Page Request DTO: {}", pageRequestDTO);
		log.info("Response DTO: {}", responseDTO);

		model.addAttribute("pageRequestDTO", pageRequestDTO);
		model.addAttribute("responseDTO", responseDTO);

		return "my/userScrapsActivity";
	}

}
