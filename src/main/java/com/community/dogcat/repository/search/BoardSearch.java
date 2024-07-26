package com.community.dogcat.repository.search;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.community.dogcat.dto.board.BoardListDTO;
import com.community.dogcat.dto.home.HomeGeneralListDTO;
import com.community.dogcat.dto.home.HomeQnaListDTO;
import com.community.dogcat.dto.home.HomeShowOffListDTO;
import com.community.dogcat.dto.home.HomeTipListDTO;
import com.community.dogcat.dto.home.HomeTodayListDTO;
import com.community.dogcat.dto.myPage.activity.UserPostsActivityDTO;
import com.community.dogcat.dto.home.search.AllSearchDTO;

public interface BoardSearch {

	// 통합 검색 : 첨부파일 유/무 + 비밀글 제외
	Page<AllSearchDTO> searchAll(String[] types, String keyword, Pageable pageable);

	// 게시판 : 게시물에 달린 댓글수 + 첨부파일 유/무 + boardCode정렬 (readDetial list에 사용)
	Page<BoardListDTO> listWithBoard(Pageable pageable, String boardCode);

	// 게시판 : 게시물에 달린 댓글수 + 첨부파일 유/무 + boardCode정렬 + 정렬기준선택가능
	Page<BoardListDTO> listWithAll(String[] types, String keyword, Pageable pageable, String boardCode, String PostTag,
		String order);

	// 마이 페이지 : 회원이 작성한 게시물 검색 및 페이징
	Page<UserPostsActivityDTO> postListWithUser(String[] types, String keyword, Pageable pageable, String userId);

	// 홈 : 실시간 인기 게시글 리스트
	List<HomeTodayListDTO> todayList(Instant startOfDay, Instant endOfDay, int size);

	// 홈 : showOff 리스트
	List<HomeShowOffListDTO> showOffList(int size);

	// 홈 : general 리스트
	List<HomeGeneralListDTO> generalList(int size);

	// 홈 : tip 리스트
	List<HomeTipListDTO> tipList(int size);

	// 홈 : qna 리스트
	List<HomeQnaListDTO> qnaList(int size);

}
