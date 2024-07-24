package com.community.dogcat.service.sample;

import java.util.List;

import com.community.dogcat.dto.sample.home.SampleGeneralListDTO;
import com.community.dogcat.dto.sample.home.SampleQnaListDTO;
import com.community.dogcat.dto.sample.home.SampleShowOffListDTO;
import com.community.dogcat.dto.sample.home.SampleTipListDTO;
import com.community.dogcat.dto.sample.home.SampleTodayListDTO;
import com.community.dogcat.dto.sample.search.AllSearchDTO;
import com.community.dogcat.dto.sample.search.SamplePageRequestDTO;
import com.community.dogcat.dto.sample.search.SampleResponseDTO;

public interface SampleService {

	// 통합 검색 : 첨부파일 유/무 + 비밀글 제외
	SampleResponseDTO<AllSearchDTO> searchAll(SamplePageRequestDTO pageRequestDTO);

	// 실시간 인기 게시글 리스트
	List<SampleTodayListDTO> getPostsForToday();

	// showOff 게시판 리스트
	List<SampleShowOffListDTO> getShowOffPosts();

	// general 게시판 리스트
	List<SampleGeneralListDTO> getGeneralPosts();

	// tip 게시판 리스트
	List<SampleTipListDTO> getTipPosts();

	// qna 게시판 리스트
	List<SampleQnaListDTO> getQnaPosts();

}

