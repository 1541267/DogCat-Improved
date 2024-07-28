package com.community.dogcat.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.community.dogcat.dto.home.HomeGeneralListDTO;
import com.community.dogcat.dto.home.HomeQnaListDTO;
import com.community.dogcat.dto.home.HomeShowOffListDTO;
import com.community.dogcat.dto.home.HomeTipListDTO;
import com.community.dogcat.dto.home.HomeTodayListDTO;
import com.community.dogcat.dto.home.search.AllSearchDTO;
import com.community.dogcat.dto.home.search.HomePageRequestDTO;
import com.community.dogcat.dto.home.search.HomeResponseDTO;
import com.community.dogcat.jwt.JWTUtil;
import com.community.dogcat.service.home.HomeService;
import com.community.dogcat.service.user.UserService;

@Controller
public class HomeController extends BaseController {

	private final HomeService homeService;

	public HomeController(JWTUtil jwtUtil,
		UserService userService, HomeService homeService) {
		super(jwtUtil, userService);
		this.homeService = homeService;
	}

	@GetMapping("/")
	public String home(Model model) {

		List<HomeTodayListDTO> todayList = homeService.getPostsForToday();
		List<HomeShowOffListDTO> showOffList = homeService.getShowOffPosts();
		List<HomeGeneralListDTO> generalList = homeService.getGeneralPosts();
		List<HomeTipListDTO> tipList = homeService.getTipPosts();
		List<HomeQnaListDTO> qnaList = homeService.getQnaPosts();

		model.addAttribute("todayList", todayList);
		model.addAttribute("showOffList", showOffList);
		model.addAttribute("generalList", generalList);
		model.addAttribute("tipList", tipList);
		model.addAttribute("qnaList", qnaList);

		Boolean isLoggedIn = (Boolean) model.getAttribute("isLoggedIn");

		if (isLoggedIn != null && isLoggedIn) {

			String username = (String) model.getAttribute("username");

			long userExp = userService.findUserExpByUsername(username);

			Map<String, Object> userLevelInfo = userService.checkUserLevel(userExp);

			model.addAttribute("userLevelInfo", userLevelInfo);
		}

		return "home/home";  // home.html 뷰를 반환
	}

	// 통합검색
	@GetMapping("/allSearch")
	public String allSearch(HomePageRequestDTO pageRequestDTO, Model model) {
		HomeResponseDTO<AllSearchDTO> responseDTO = homeService.searchAll(pageRequestDTO);
		model.addAttribute("searchPageRequestDTO", pageRequestDTO);
		model.addAttribute("searchResponseDTO", responseDTO);

		return "home/allSearch";
	}

	@GetMapping("/notice")
	public String notice() {

		return "home/notice";
	}
}
