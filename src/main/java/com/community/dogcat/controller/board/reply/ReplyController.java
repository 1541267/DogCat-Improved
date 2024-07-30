package com.community.dogcat.controller.board.reply;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.dto.board.reply.ReplyDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.board.reply.ReplyService;
import com.community.dogcat.service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/replies")
@Log4j2
public class ReplyController extends BaseController {

	private final ReplyService replyService;

	@Autowired
	public ReplyController(JWTUtil jwtUtil, UserService userService, ReplyService replyService) {
		super(jwtUtil, userService);
		this.replyService = replyService;
	}

	@Operation(summary = "Reply Register", description = "댓글 등록")
	@PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Long> register(@Valid @RequestBody ReplyDTO replyDTO, BindingResult bindingResult,
		Model model) throws
		BindException {
		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		}

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");
		replyDTO.setUserId(userId);

		Map<String, Long> resultMap = new HashMap<>();
		Long replyNo = replyService.register(replyDTO);

		resultMap.put("replyNo", replyNo);

		return resultMap;
	}

	@Operation(summary = "Replies of Post", description = "특정 게시물의 댓글 목록")
	@GetMapping("/{postNo}")
	public List<ReplyDTO> getList(@PathVariable("postNo") Long postNo, Model model) {

		List<ReplyDTO> list = replyService.getListOfReply(postNo);

		model.addAttribute("replies", list);

		return list;
	}

	@Operation(summary = "Delete Reply", description = "특정 댓글 삭제")
	@DeleteMapping("/{replyNo}")
	public Map<String, Long> delete(@PathVariable("replyNo") Long replyNo, Model model) {

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");

		replyService.delete(replyNo, userId);

		Map<String, Long> resultMap = new HashMap<>();

		resultMap.put("replyNo", replyNo);

		return resultMap;

	}
}
