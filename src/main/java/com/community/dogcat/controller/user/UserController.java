package com.community.dogcat.controller.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.community.dogcat.domain.User;
import com.community.dogcat.domain.UsersVet;
import com.community.dogcat.dto.user.JoinDTO;
import com.community.dogcat.service.user.UserService;
import com.community.dogcat.service.user.VetService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService joinService;
	private final UserService userService;
	private final VetService vetService;

	@GetMapping("/login")
	public void login() {
	}

	@GetMapping("/join")
	public void join() {
	}

	@PostMapping("/join")
	public String joinProc(JoinDTO dto, RedirectAttributes redirectAttributes, HttpServletRequest request) {

		String vetName = dto.getUserName();
		// Request parameter 가져오기
		String vetLicenseStr = request.getParameter("vetLicense");

		joinService.joinProcess(dto);

		User isVet = userService.findUserId(dto.getUserId());

		// Optional을 사용하여 vetLicenseStr을 Long으로 변환하고 빈값 또는 null 처리
		Long vetLicense = Optional.ofNullable(vetLicenseStr)
			.filter(s -> !s.isEmpty())
			.map(Long::valueOf)
			.orElse(null); // 빈 문자열이거나 null 인 경우 null 로 설정
		UsersVet vet = vetService.findByVetNameAndVetLicense(vetName, vetLicense);

		if (vet != null && !vet.isVerificationStatus() && isVet.isUserVet()) {

			vet.setVerificationStatus(true);
			vetService.save(vet);

		}

		redirectAttributes
			.addFlashAttribute("message", "환영합니다, " + dto.getNickname() + "님!");

		return "redirect:/user/login";
	}

	@GetMapping("/find-id")
	public void findId() {
	}

	@PostMapping("/find-id")
	public ResponseEntity<Object> findId(HttpServletRequest request) {

		String name = request.getParameter("name");
		String tel = request.getParameter("tel");

		User user = userService.findUserByNameAndTel(name, tel);

		return getObjectResponseEntity(user);

	}

	@GetMapping("/find-pw")
	public void findPw() {
	}

	@PostMapping("/find-pw")
	public ResponseEntity<Object> findPw(HttpServletRequest request) {

		String name = request.getParameter("name");
		String userId = request.getParameter("userId");

		User user = userService.findPw(name, userId);

		return getObjectResponseEntity(user);

	}

	private ResponseEntity<Object> getObjectResponseEntity(User user) {
		Map<String, Object> response = new HashMap<>();
		if (user != null) {
			response.put("isVerified", true);
			response.put("userId", user.getUserId());

			return ResponseEntity.ok().body(response);

		} else {
			response.put("isVerified", false);

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

}
