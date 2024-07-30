package com.community.dogcat.controller.board.postLike;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.dto.board.postLike.PostLikeDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.board.postLike.PostLikeService;
import com.community.dogcat.service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.log4j.Log4j2;

@Log4j2
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
	public Map<String, Long> register(@Valid @RequestBody PostLikeDTO postLikeDTO, Model model) {

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");
		postLikeDTO.setUserId(userId);

		Map<String, Long> response = new HashMap<>();
		Long likeNo = postLikeService.register(postLikeDTO);

		response.put("likeNo", likeNo);

		return response;
	}

	@Operation(summary = "like Delete", description = "게시물 좋아요/싫어요 취소")
	@DeleteMapping("/{likeNo}")
	public Map<String, Long> delete(@PathVariable("likeNo") Long likeNo, Model model) {

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");

		postLikeService.delete(likeNo, userId);

		Map<String, Long> response = new HashMap<>();

		response.put("likeNo", likeNo);

		return response;
	}

}
