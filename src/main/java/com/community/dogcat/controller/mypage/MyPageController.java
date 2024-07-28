package com.community.dogcat.controller.mypage;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	public String userModifyConfirm(@ModelAttribute UserDetailDTO dto, HttpServletResponse response, RedirectAttributes redirectAttributes) {

		boolean needsLogout = userService.userModify(dto);

		if (needsLogout) {

			clearCookies(response);

			redirectAttributes
				.addFlashAttribute("message", "회원수정이 완료되었습니다. 다시 로그인해주세요.");

			return "redirect:/user/login";

		}

		return "redirect:/my/user-detail";

	}


	@PostMapping("/delete-user")
	public String deleteUser(HttpServletResponse response, @RequestParam("userId") String userId, RedirectAttributes redirectAttributes) {

		userService.deleteUserById(response, userId);

		redirectAttributes.addFlashAttribute("message", "회원탈퇴가 완료되었습니다.");

		return "redirect:/user/login";

	}

	private void clearCookies(HttpServletResponse response) {

		Cookie refreshCookie = new Cookie("refresh", null);
		refreshCookie.setMaxAge(0);
		refreshCookie.setPath("/");

		Cookie accessCookie = new Cookie("access", null);
		accessCookie.setMaxAge(0);
		accessCookie.setPath("/");

		Cookie sessionCookie = new Cookie("JSESSIONID", null);
		sessionCookie.setMaxAge(0);
		sessionCookie.setPath("/");

		response.addCookie(refreshCookie);
		response.addCookie(accessCookie);
		response.addCookie(sessionCookie);

	}

}
