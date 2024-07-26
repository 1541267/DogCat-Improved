package com.community.dogcat.service.home;

import java.util.List;

import com.community.dogcat.dto.home.HomeGeneralListDTO;
import com.community.dogcat.dto.home.HomeQnaListDTO;
import com.community.dogcat.dto.home.HomeShowOffListDTO;
import com.community.dogcat.dto.home.HomeTipListDTO;
import com.community.dogcat.dto.home.HomeTodayListDTO;
import com.community.dogcat.dto.home.search.AllSearchDTO;
import com.community.dogcat.dto.home.search.HomePageRequestDTO;
import com.community.dogcat.dto.home.search.HomeResponseDTO;

public interface HomeService {

	// 통합 검색 : 첨부파일 유/무 + 비밀글 제외
	HomeResponseDTO<AllSearchDTO> searchAll(HomePageRequestDTO pageRequestDTO);

	// 실시간 인기 게시글 리스트
	List<HomeTodayListDTO> getPostsForToday();

	// showOff 게시판 리스트
	List<HomeShowOffListDTO> getShowOffPosts();

	// general 게시판 리스트
	List<HomeGeneralListDTO> getGeneralPosts();

	// tip 게시판 리스트
	List<HomeTipListDTO> getTipPosts();

	// qna 게시판 리스트
	List<HomeQnaListDTO> getQnaPosts();

}

