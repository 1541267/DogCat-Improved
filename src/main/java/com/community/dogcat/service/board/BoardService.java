package com.community.dogcat.service.board;

import java.util.List;

import org.springframework.stereotype.Service;

import com.community.dogcat.domain.Post;
import com.community.dogcat.dto.board.BoardListDTO;
import com.community.dogcat.dto.board.BoardPageRequestDTO;
import com.community.dogcat.dto.board.BoardPageResponseDTO;
import com.community.dogcat.dto.board.PostReadDTO;
import com.community.dogcat.dto.board.post.PostDTO;

public interface BoardService {

	Long register(PostDTO postDTO, List<String> uuids);

	void delete(Long postNo, String userId);

	// 게시글 수정용 게시글 보기
	PostDTO readOne(Long postNo);

	// 게시글 상세보기 접속시 조회수 증가
	void updateViewCount(Long postNo);

	// 게시글 상세보기
	PostReadDTO readDetail(Long postNo, String userId);

	Long modify(PostDTO postDTO, String userId);

	// 수의사 답변글 답변 완료
	Long completeQna(PostDTO postDTO, String userId);

	// readDetail 하단 list
	BoardPageResponseDTO<BoardListDTO> readList(BoardPageRequestDTO pageRequestDTO);

	// regDate(최신순), boarCode에 따라 정렬, 첨부파일 정보 추가 + 정렬기준선택가능
	BoardPageResponseDTO<BoardListDTO> list(BoardPageRequestDTO pageRequestDTO);

	Post findPostByPostNo(Long postNo);

	List<String> getImages(Long postNo);

}

