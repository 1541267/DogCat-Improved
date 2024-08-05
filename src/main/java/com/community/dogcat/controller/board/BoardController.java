package com.community.dogcat.controller.board;

import static org.springframework.http.HttpStatus.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.community.dogcat.controller.BaseController;
import com.community.dogcat.domain.Post;
import com.community.dogcat.dto.board.BoardListDTO;
import com.community.dogcat.dto.board.BoardPageRequestDTO;
import com.community.dogcat.dto.board.BoardPageResponseDTO;
import com.community.dogcat.dto.board.PostReadDTO;
import com.community.dogcat.dto.board.post.PostDTO;
import com.community.dogcat.dto.board.postLike.PostLikeDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.board.BoardService;
import com.community.dogcat.service.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@Transactional
@RequestMapping("/board")
public class BoardController extends BaseController {

	@Value("${s3UploadedUrl}")
	private String s3UploadedUrl;

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

		if (userId == null) {
			log.error("BoardController Register Error : 401 Unauthorized");
			return ResponseEntity.status(UNAUTHORIZED).build(); // 인증되지 않은 경우(로그인 필요)
		}

		postDTO.setUserId(userId);

		Long id = boardService.register(postDTO);

		Map<String, Long> response = new HashMap<>();
		response.put("postNo", id);

		// 게시글 등록 먼저, postNo을 response 로 담아  summernote 로 받고 이미지 업로드 진행
		return ResponseEntity.ok(response);
	}

	@GetMapping("/read/{postNo}")
	public String read(@PathVariable Long postNo, BoardPageRequestDTO pageRequestDTO,
		Model model) {

		// 게시글 유무 - ys
		Post post = boardService.findPostByPostNo(postNo);
		if (post == null) {
			log.error("BoardController Read Error : 404 Not Found");
			return "redirect:/error"; // 게시글이 존재하지 않는 경우 404 오류
		}

		// 조회수 증가
		boardService.updateViewCount(postNo);

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");
		String role = userService.getRole(userId);

		// 로그인 사용자 확인
		if (userId == null) {
			log.error("BoardController Read Error : 401 Unauthorized");
			return "redirect:/user/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
		}

		// 상세페이지 출력
		PostReadDTO postDTO = boardService.readDetail(postNo, userId);

		// 게시글 정보
		model.addAttribute("postDTO", postDTO);
		boolean secret = postDTO.isSecret();

		// 비밀글 게시글 작성자 확인
		if (secret) {
			if(!userId.equals(postDTO.getUserId()) && role.equals("ROLE_USER")) {
				log.error("BoardController Read Error : 403 Forbidden");
				return "redirect:/error"; // 권한이 없는 경우 403 오류
			}
		}

		// postDTO에서 boardCode 추출
		String boardCode = postDTO.getBoardCode();
		// pageRequestDTO에 받아온 boardCode 값을 설정
		pageRequestDTO.setBoardCode(boardCode);

		BoardPageResponseDTO<BoardListDTO> responseDTO = boardService.readList(pageRequestDTO);

		model.addAttribute("pageRequestDTO", pageRequestDTO);
		model.addAttribute("responseDTO", responseDTO);

		// 이미지 정보
		List<String> uploadPaths = boardService.getImages(postNo);

		model.addAttribute("uploadPaths", uploadPaths);

		return "board/read";
	}

	@GetMapping({"/modify/{postNo}", "/modify_q/{postNo}"})
	public String modify(@PathVariable Long postNo, Model model, HttpServletRequest request) {

		PostDTO postDTO = boardService.readOne(postNo);

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");

		// 로그인 사용자 확인
		if (userId == null) {
			log.error("BoardController Modify Error : 401 Unauthorized");
			return "redirect:/user/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
		}

		// 게시글 조회
		if (postNo == null) {
			log.error("BoardController Modify Error : 404 Not Found");
			return "redirect:/error"; // 게시글이 존재하지 않는 경우 404 오류
		}

		// 게시글 작성자 확인
		if (!userId.equals(postDTO.getUserId())) {
			log.error("BoardController Modify Error : 403 Forbidden");
			return "redirect:/error"; // 권한이 없는 경우 403 오류
		}

		model.addAttribute("s3UploadedUrl", s3UploadedUrl);
		model.addAttribute("postDTO", postDTO);

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

		// 게시글 조회
		Post post = boardService.findPostByPostNo(postDTO.getPostNo());
		if (post == null) {
			log.error("BoardController Modify Post Error : 404 Not Found");
			return ResponseEntity.status(NOT_FOUND).build(); // 게시글이 존재하지 않는 경우 404 오류
		}

		// 로그인 사용자 확인
		if (userId == null) {
			log.error("BoardController Modify Post Error : 401 Unauthorized");
			return ResponseEntity.status(UNAUTHORIZED).build(); // 로그인되지 않은 경우 401 오류
		}

		// 게시글 작성자 확인
		if (!userId.equals(postDTO.getUserId())) {
			log.error("BoardController Modify Post Error : 403 Forbidden");
			return ResponseEntity.status(FORBIDDEN).build(); // 권한이 없는 경우 403 오류
		}

		Long id = boardService.modify(postDTO, userId);

		Map<String, Long> response = new HashMap<>();

		response.put("modifyPostNo", id);

		return ResponseEntity.ok(response);
	}

	@PostMapping("modify_q")
	public ResponseEntity<Map<String, Long>> modify_q(@ModelAttribute PostDTO postDTO, Model model) {

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");

		// 게시글 조회
		Post post = boardService.findPostByPostNo(postDTO.getPostNo());
		if (post == null) {
			log.error("BoardController Modify_q Post Error : 404 Not Found");
			return ResponseEntity.status(NOT_FOUND).build(); // 게시글이 존재하지 않는 경우 404 오류
		}

		// 로그인 사용자 확인
		if (userId == null) {
			log.error("BoardController Modify_q Post Error : 401 Unauthorized");
			return ResponseEntity.status(UNAUTHORIZED).build(); // 로그인되지 않은 경우 401 오류
		}

		// 게시글 작성자 확인
		if (!userId.equals(postDTO.getUserId())) {
			log.error("BoardController Modify_q Post Error : 403 Forbidden");
			return ResponseEntity.status(FORBIDDEN).build(); // 권한이 없는 경우 403 오류
		}

		Long id = boardService.modify(postDTO, userId);

		Map<String, Long> response = new HashMap<>();

		response.put("modifyPostNo", id);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/completeQna")
	public ResponseEntity<Map<String, Long>> completeQna (@RequestBody PostDTO postDTO, Model model) {

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");

		// 게시글 조회
		Post post = boardService.findPostByPostNo(postDTO.getPostNo());
		if (post == null) {
			log.error("BoardController completeQna Error : 404 Not Found");
			return ResponseEntity.status(NOT_FOUND).build(); // 게시글이 존재하지 않는 경우 404 오류
		}

		// 로그인 사용자 확인
		if (userId == null) {
			log.error("BoardController completeQna Post Error : 401 Unauthorized");
			return ResponseEntity.status(UNAUTHORIZED).build(); // 로그인되지 않은 경우 403 오류
		}

		// 게시글 작성자 확인
		if (!userId.equals(postDTO.getUserId())) {
			log.error("BoardController completeQna Post Error : 403 Forbidden");
			return ResponseEntity.status(FORBIDDEN).build(); // 권한이 없는 경우 403 오류
		}

		Long postNo = boardService.completeQna(postDTO, userId);

		Map<String, Long> response = new HashMap<>();

		response.put("postNo", postNo);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/delete/{postNo}")
	public String delete(@PathVariable Long postNo, Model model) {

		// 모델에서 사용자 정보를 가져옴
		String userId = (String)model.getAttribute("username");
		String role = userService.getRole(userId);

		// 로그인 사용자 확인
		if (userId == null) {
			log.error("BoardController Delete Error : 401 Unauthorized");
			return "redirect:/user/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
		}

		// 게시글 조회
		PostDTO postDTO = boardService.readOne(postNo);
		if (postNo == null) {
			log.error("BoardController Delete Error : 404 Not Found");
			return "redirect:/error"; // 게시글이 존재하지 않는 경우 404 오류
		}

		// 게시글 작성자 확인
		if (!userId.equals(postDTO.getUserId()) && !role.equals("ROLE_ADMIN")) {
			log.error("BoardController Delete Error : 403 Forbidden");
			return "redirect:/error"; // 권한이 없는 경우 403 오류
		}

		// 삭제시 해당 게시판 목록으로 돌아가기 위해 boardCode 저장
		String boardCode = postDTO.getBoardCode();

		boardService.delete(postNo, userId);

		return "redirect:/board/" + boardCode;
	}

	//게시판별목록
	@GetMapping("/{boardCode}")
	public String list(@PathVariable String boardCode, BoardPageRequestDTO pageRequestDTO, Model model) {

		model.addAttribute("boardCode", boardCode);

		BoardPageResponseDTO<BoardListDTO> responseDTO = boardService.list(pageRequestDTO);

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
