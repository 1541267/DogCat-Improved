package com.community.dogcat.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.community.dogcat.dto.sample.home.SampleGeneralListDTO;
import com.community.dogcat.dto.sample.home.SampleQnaListDTO;
import com.community.dogcat.dto.sample.home.SampleShowOffListDTO;
import com.community.dogcat.dto.sample.home.SampleTipListDTO;
import com.community.dogcat.dto.sample.home.SampleTodayListDTO;
import com.community.dogcat.dto.sample.search.AllSearchDTO;
import com.community.dogcat.dto.sample.search.SamplePageRequestDTO;
import com.community.dogcat.dto.sample.search.SampleResponseDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.sample.SampleService;
import com.community.dogcat.service.user.UserService;

@Controller
@RequestMapping("/sample")
public class SampleController extends BaseController {

	private final SampleService sampleService;

	public SampleController(JWTUtil jwtUtil,
		UserService userService, SampleService sampleService) {
		super(jwtUtil, userService);
		this.sampleService = sampleService;
	}

	@GetMapping("/home")
	public String home(Model model) {

		List<SampleTodayListDTO> todayList = sampleService.getPostsForToday();
		List<SampleShowOffListDTO> showOffList = sampleService.getShowOffPosts();
		List<SampleGeneralListDTO> generalList = sampleService.getGeneralPosts();
		List<SampleTipListDTO> tipList = sampleService.getTipPosts();
		List<SampleQnaListDTO> qnaList = sampleService.getQnaPosts();

		model.addAttribute("todayList", todayList);
		model.addAttribute("showOffList", showOffList);
		model.addAttribute("generalList", generalList);
		model.addAttribute("tipList", tipList);
		model.addAttribute("qnaList", qnaList);

		String username = (String)model.getAttribute("username");

		long userExp = userService.findUserExpByUsername(username);

		// 유저 레벨과 퍼센티지 계산
		Map<String, Object> userLevelInfo = userService.checkUserLevel(userExp);

		// 모델에 추가
		model.addAttribute("userLevelInfo", userLevelInfo);

		return "/sample/home";  // home.html 뷰를 반환
	}

	// 통합검색
	@GetMapping("/allSearch")
	public void allSearch(SamplePageRequestDTO pageRequestDTO, Model model) {
		SampleResponseDTO<AllSearchDTO> responseDTO = sampleService.searchAll(pageRequestDTO);
		model.addAttribute("searchPageRequestDTO", pageRequestDTO);
		model.addAttribute("searchResponseDTO", responseDTO);
	}

	@GetMapping("/notice")
	public void notice() {

	}
}
