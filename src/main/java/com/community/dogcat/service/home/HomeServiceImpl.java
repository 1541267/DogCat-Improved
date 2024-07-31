package com.community.dogcat.service.home;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.dto.home.HomeGeneralListDTO;
import com.community.dogcat.dto.home.HomeQnaListDTO;
import com.community.dogcat.dto.home.HomeShowOffListDTO;
import com.community.dogcat.dto.home.HomeTipListDTO;
import com.community.dogcat.dto.home.HomeTodayListDTO;
import com.community.dogcat.dto.home.search.AllSearchDTO;
import com.community.dogcat.dto.home.search.HomePageRequestDTO;
import com.community.dogcat.dto.home.search.HomeResponseDTO;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.board.reply.ReplyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

	private final BoardRepository boardRepository;

	private final ReplyRepository replyRepository;

	// 통합 검색 : 첨부파일 유/무 + 비밀글 제외
	@Override
	public HomeResponseDTO<AllSearchDTO> searchAll(HomePageRequestDTO pageRequestDTO) {

		String[] types = pageRequestDTO.getTypes();
		String keyword = pageRequestDTO.getKeyword();
		Pageable pageable = pageRequestDTO.getPageable();

		Page<AllSearchDTO> result = boardRepository.searchAll(types, keyword, pageable);

		// 추가할 정보 설정
		List<AllSearchDTO> dtoList = result.getContent().stream()
			.peek(dto -> {
				// 포스트 하나에 달린 댓글 수 설정
				Long replyCount = replyRepository.countRepliesByPost(dto.getPostNo());
				dto.setReplyCount(replyCount != null ? replyCount : 0L);
			})
			.collect(Collectors.toList());

		return HomeResponseDTO.<AllSearchDTO>withAll()
			.pageRequestDTO(pageRequestDTO)
			.dtoList(dtoList)
			.total((int)result.getTotalElements())
			.keyword(pageRequestDTO.getKeyword())
			.build();
	}

	// 실시간 인기 게시글 리스트
	public List<HomeTodayListDTO> getPostsForToday() {

		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		Instant startOfDay = today.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
		Instant endOfDay = today.atTime(23, 59, 59).atZone(ZoneId.of("Asia/Seoul")).toInstant();

		return boardRepository.todayList(startOfDay, endOfDay, 10);
	}

	// showOff 게시글 리스트
	public List<HomeShowOffListDTO> getShowOffPosts() {
		return boardRepository.showOffList(3);
	}

	// general 게시글 리스트
	public List<HomeGeneralListDTO> getGeneralPosts() {
		return boardRepository.generalList(10);
	}

	// tip 게시글 리스트
	public List<HomeTipListDTO> getTipPosts() {
		return boardRepository.tipList(10);
	}

	// qna 게시글 리스트
	public List<HomeQnaListDTO> getQnaPosts() {
		return boardRepository.qnaList(10);
	}

}

