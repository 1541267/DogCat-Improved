package com.community.dogcat.controller.board.postLike;

import static org.springframework.http.HttpStatus.*;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.domain.PostLike;
import com.community.dogcat.dto.board.postLike.PostLikeDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.board.postLike.PostLikeService;
import com.community.dogcat.service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/postLike")
public class PostLikeController extends BaseController {

	private final PostLikeService postLikeService;

	@Autowired
	public PostLikeController(JWTUtil jwtUtil, UserService userService, PostLikeService postLikeService) {
		super(jwtUtil, userService);
		this.postLikeService = postLikeService;
	}

	@Operation(summary = "like register", description = "게시물 좋아요/싫어요 등록")
	@PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Long>> register(@Valid @RequestBody PostLikeDTO postLikeDTO, Model model) {

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");

		// 로그인 사용자 확인
		if (userId == null) {
			log.error("PostLikeController register Error : 401 Unauthorized");
			return ResponseEntity.status(UNAUTHORIZED).build(); // 로그인되지 않은 경우 401 오류
		}

		Map<String, Long> response = new HashMap<>();

		Long likeNo = postLikeService.register(postLikeDTO);

		response.put("likeNo", likeNo);

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "like Delete", description = "게시물 좋아요/싫어요 취소")
	@DeleteMapping("/{likeNo}")
	public ResponseEntity<Map<String, Long>> delete(@PathVariable("likeNo") Long likeNo, Model model) {

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");

		// 로그인 사용자 확인
		if (userId == null) {
			log.error("PostLikeController Delete Error : 401 Unauthorized");
			return ResponseEntity.status(UNAUTHORIZED).build(); // 로그인되지 않은 경우 401 오류
		}

		// 좋아요/싫어요 조회
		if (likeNo == null) {
			log.error("PostLikeController Delete Error : 404 Not Found");
			return ResponseEntity.status(NOT_FOUND).build(); // 좋아요가 존재하지 않는 경우 404 오류
		}

		postLikeService.delete(likeNo, userId);

		Map<String, Long> response = new HashMap<>();

		response.put("likeNo", likeNo);

		return ResponseEntity.ok(response);
	}

}
