package com.community.dogcat.controller.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.domain.Post;
import com.community.dogcat.dto.board.BoardListDTO;
import com.community.dogcat.dto.board.BoardPageRequestDTO;
import com.community.dogcat.dto.board.BoardPageResponseDTO;
import com.community.dogcat.dto.board.PostReadDTO;
import com.community.dogcat.dto.board.post.PostDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.board.BoardService;
import com.community.dogcat.service.user.UserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@Transactional
@RequestMapping("/board")
public class BoardController extends BaseController {

	private final BoardService boardService;

	@Autowired
	public BoardController(JWTUtil jwtUtil, UserService userService, BoardService boardService) {
		super(jwtUtil, userService);
		this.boardService = boardService;
	}

	@GetMapping({"/register", "/register_q"})
	public void register() {
	}

	@ResponseBody
	@PostMapping({"/register", "/register_q"})
	public ResponseEntity<Map<String, Long>> register(@ModelAttribute PostDTO postDTO, Model model) {
		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");
		log.info("-----------------------userId" + userId);

		if (userId == null) {
			return ResponseEntity.status(401).build(); // 인증되지 않은 경우(로그인 필요)
		}

		postDTO.setUserId(userId);

		log.info("-------- register postDTO: {}", postDTO);
		Long id = boardService.register(postDTO);

		Map<String, Long> response = new HashMap<>();
		response.put("postNo", id);

		log.info("----------------- register postNo: {}", id);
		log.info("----------------- register postContent: {}", postDTO.getPostContent());
		// 게시글 등록 먼저, postNo을 response 로 담아  summernote 로 받고 이미지 업로드 진행
		return ResponseEntity.ok(response);
	}

	@GetMapping("/read/{postNo}")
	public String read(@PathVariable Long postNo, BoardPageRequestDTO pageRequestDTO,
		Model model) {
		// 게시글 유무 - ys
		Post post = boardService.findPostByPostNo(postNo);
		log.info("-------------------: read postNo: {}", postNo);
		// 조회수 증가
		boardService.updateViewCount(postNo);
		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");
		// 상세페이지 출력
		PostReadDTO postDTO = boardService.readDetail(postNo, userId);

		// 게시글 정보
		model.addAttribute("postDTO", postDTO);

		// postDTO에서 boardCode 추출
		String boardCode = postDTO.getBoardCode();
		log.info("boardCode: {}", boardCode);
		// pageRequestDTO에 받아온 boardCode 값을 설정
		pageRequestDTO.setBoardCode(boardCode);

		BoardPageResponseDTO<BoardListDTO> responseDTO = boardService.readList(pageRequestDTO);
		// log.info(responseDTO);

		model.addAttribute("pageRequestDTO", pageRequestDTO);
		model.addAttribute("responseDTO", responseDTO);

		if (post != null) {
			// 이미지 정보
			List<String> uploadPaths = boardService.getImages(postNo);
			model.addAttribute("uploadPaths", uploadPaths);
			model.addAttribute("post", post);

			return "board/read";

		} else {
			return "/error/404"; // 해당 게시글이 없을경우, 에러 페이지 만드는게 어떨지?
		}
	}

	@GetMapping({"/modify/{postNo}", "/modify_q/{postNo}"})
	public String modify(@PathVariable Long postNo, Model model, HttpServletRequest request) {
		PostDTO postDTO = boardService.readOne(postNo);
		log.info(postDTO);

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");
		log.info("-----------------------userId" + userId);

		// 로그인 사용자 확인
		if (userId == null) {
			return "redirect:/user/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
		}

		// 게시글 작성자 확인
		if (!userId.equals(postDTO.getUserId())) {
			return "redirect:/error/403"; // 권한이 없는 경우 403 오류 페이지로 리다이렉트
		}

		model.addAttribute("postDTO", postDTO);
		log.info("modify postNo: " + postDTO.getPostNo());

		if (request.getRequestURI().contains("/modify_q")) {
			return "board/modify_q";
		} else {
			return "board/modify";
		}
	}

	@PostMapping("modify")
	public ResponseEntity<Map<String, Long>> modify(@ModelAttribute PostDTO postDTO, Model model) {
		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");

		if (!userId.equals(postDTO.getUserId())) {
			return ResponseEntity.status(403).build(); // 권한이 없는 경우 403 오류 페이지로 리다이렉트
		}

		Long id = boardService.modify(postDTO);

		Map<String, Long> response = new HashMap<>();
		response.put("modifyPostNo", id);

		return ResponseEntity.ok(response);
	}

	@PostMapping("modify_q")
	public ResponseEntity<Map<String, Long>> modify_q(@ModelAttribute PostDTO postDTO, Model model) {
		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");

		if (!userId.equals(postDTO.getUserId())) {
			return ResponseEntity.status(403).build(); // 권한이 없는 경우 403 오류 페이지로 리다이렉트
		}

		Long id = boardService.modify(postDTO);

		Map<String, Long> response = new HashMap<>();
		response.put("modifyPostNo", id);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/delete/{postNo}")
	public String delete(@PathVariable Long postNo, Model model) {

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");
		String role = userService.getRole(userId);

		log.info("-----------------------userId" + userId);
		log.info("-----------------------role" + role);

		// 로그인 사용자 확인
		if (userId == null) {
			return "redirect:/user/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
		}

		// 게시글 조회
		PostDTO postDTO = boardService.readOne(postNo);
		if (postDTO == null) {
			return "redirect:/error/404"; // 게시글이 존재하지 않는 경우 404 오류 페이지로 리다이렉트
		}

		// 게시글 작성자 확인
		if (!userId.equals(postDTO.getUserId()) && !role.equals("ROLE_ADMIN")) {
			return "redirect:/error/403"; // 권한이 없는 경우 403 오류 페이지로 리다이렉트
		}
		// 삭제시 해당 게시판 목록으로 돌아가기 위해 boardCode 저장
		String boardCode = postDTO.getBoardCode();

		log.info("delete PostNo: {}", postNo);
		boardService.delete(postNo, userId);

		return "redirect:/board/" + boardCode;
	}

	//게시판별목록
	@GetMapping("/{boardCode}")
	public String list(@PathVariable String boardCode, BoardPageRequestDTO pageRequestDTO, Model model) {
		model.addAttribute("boardCode", boardCode);

		BoardPageResponseDTO<BoardListDTO> responseDTO = boardService.list(pageRequestDTO);
		log.info(responseDTO);

		model.addAttribute("pageRequestDTO", pageRequestDTO);
		model.addAttribute("responseDTO", responseDTO);

		String returnUrl;
		switch (boardCode) {
			case "general":
			case "tip":
			case "qna":
				returnUrl = "board/list";
				break;
			default:
				returnUrl = "board/showoff";
				break;
		}
		return returnUrl;
	}
}
