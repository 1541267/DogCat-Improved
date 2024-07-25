package com.community.dogcat.controller.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.community.dogcat.domain.UsersVet;
import com.community.dogcat.service.user.UserService;
import com.community.dogcat.service.user.VetService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/check")
public class JoinVerificationController {

	private final VetService vetService;
	private final UserService userService;

	@PostMapping("/userId")
	public ResponseEntity<Map<String, Boolean>> checkUserId(@RequestBody Map<String, String> requestBody) {

		String userId = requestBody.get("userId");
		boolean exists = userService.isUserIdExists(userId);

		Map<String, Boolean> response = new HashMap<>();
		response.put("exists", exists);

		return ResponseEntity.ok(response);

	}

	@PostMapping("/password")
	public ResponseEntity<Map<String, Boolean>> checkPassword(@RequestBody Map<String, String> requestBody) {

		String userId = requestBody.get("userId");
		String inputPassword = requestBody.get("password");
		boolean valid = userService.checkPassword(userId, inputPassword);

		Map<String, Boolean> response = new HashMap<>();
		response.put("valid", valid);

		return ResponseEntity.ok(response);

	}

	@PostMapping("/nickname")
	public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestBody Map<String, String> requestBody) {

		String nickname = requestBody.get("nickname");
		boolean exists = userService.isNicknameExists(nickname);

		Map<String, Boolean> response = new HashMap<>();
		response.put("exists", exists);

		return ResponseEntity.ok(response);

	}

	@PostMapping("/vet")
	public ResponseEntity<Object> verifyVet(HttpServletRequest request) {

		String vetName = request.getParameter("vetName");
		Long vetLicense = Long.valueOf(request.getParameter("vetLicense"));
		UsersVet vet = vetService.findByVetNameAndVetLicense(vetName, vetLicense);

		if (vet != null && !vet.isVerificationStatus()) {

			vet.setVerificationStatus(true);
			vetService.save(vet);

			return ResponseEntity.ok().body("{\"message\": \"수의사 인증이 완료되었습니다.\", \"isVerified\": true}");

		} else if (vet != null) {

			return ResponseEntity.ok().body("{\"message\": \"이미 인증된 수의사정보입니다.\", \"isVerified\": false}");

		} else {

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("{\"message\": \"수의사 인증에 실패하였습니다.\", \"isVerified\": false}");
		}

	}

}
