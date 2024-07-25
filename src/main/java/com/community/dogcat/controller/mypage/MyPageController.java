package com.community.dogcat.controller.mypage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.dto.user.UserDetailDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.user.UserService;

@Controller
@RequestMapping("/my")
public class MyPageController extends BaseController {

	public MyPageController(JWTUtil jwtUtil, UserService userService) {

		super(jwtUtil, userService);

	}

	@GetMapping("/user-detail")
	public void userDetail(HttpServletRequest request, Model model) {

		String accessToken = extractTokenFromCookie(request);

		if (accessToken == null) {
			model.addAttribute("user", null);
			return;
		}

		String username = jwtUtil.getUsername(accessToken);

		UserDetailDTO user = userService.findByUserId(username);
		model.addAttribute("user", user);

	}

	@GetMapping("/user-modify")
	public void userModify(@RequestParam("userId") String userId, Model model) {

		UserDetailDTO user = userService.findByUserId(userId);
		model.addAttribute("user", user);

	}

	@PostMapping("/user-modify")
	public String userModifyConfirm(@ModelAttribute UserDetailDTO dto) {

		userService.userModify(dto);
		return "redirect:/my/user-detail";

	}

	@PostMapping("/delete-user")
	public String deleteUser(HttpServletResponse response, @RequestParam("userId") String userId) {

		userService.deleteUserById(response, userId);
		return "redirect:/user/login";

	}

}
