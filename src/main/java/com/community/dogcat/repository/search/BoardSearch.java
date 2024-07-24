package com.community.dogcat.repository.search;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.community.dogcat.dto.board.BoardListDTO;
import com.community.dogcat.dto.myPage.activity.UserPostsActivityDTO;
import com.community.dogcat.dto.sample.home.SampleGeneralListDTO;
import com.community.dogcat.dto.sample.home.SampleQnaListDTO;
import com.community.dogcat.dto.sample.home.SampleShowOffListDTO;
import com.community.dogcat.dto.sample.home.SampleTipListDTO;
import com.community.dogcat.dto.sample.home.SampleTodayListDTO;
import com.community.dogcat.dto.sample.search.AllSearchDTO;

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
	List<SampleTodayListDTO> todayList(Instant startOfDay, Instant endOfDay, int size);

	// 홈 : showOff 리스트
	List<SampleShowOffListDTO> showOffList(int size);

	// 홈 : general 리스트
	List<SampleGeneralListDTO> generalList(int size);

	// 홈 : tip 리스트
	List<SampleTipListDTO> tipList(int size);

	// 홈 : qna 리스트
	List<SampleQnaListDTO> qnaList(int size);

}
