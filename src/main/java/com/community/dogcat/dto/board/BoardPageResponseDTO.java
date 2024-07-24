package com.community.dogcat.dto.board;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BoardPageResponseDTO<E> {

	private int page;
	private int total;

	// 페이지당 표시되는 갯수
	private int size;

	// 페이징 이동 시작,끝 번호
	private int start;
	private int end;

	// 이전,이후 페이지 존재 여부
	private boolean prev;
	private boolean next;

	// 최초, 마지막 페이지
	private int first = 1;
	private int last;

	// DTO->List
	private List<E> dtoList;

	@Builder(builderMethodName = "withAll")
	public BoardPageResponseDTO(BoardPageRequestDTO pageRequestDTO, List<E> dtoList, int total) {

		this.page = pageRequestDTO.getPage();
		this.size = pageRequestDTO.getSize();

		this.total = total;
		this.dtoList = dtoList;

		// 화면상 마지막번호
		this.end = (int)(Math.ceil(this.page / 5.0)) * 5;

		// 화면상 시작번호
		this.start = this.end - 4;

		// 데이터 개수상 마지막 페이지 번호
		this.last = (int)(Math.ceil((total / (double)size)));

		// end 조건
		this.end = end > last ? last : end;

		// prev 조건
		this.prev = this.start > 1;

		// next 조건
		this.next = total > this.end * this.size;

		if (total <= 0) {
			this.dtoList = Collections.emptyList();
			this.start = 1;
			this.end = 1;
		}
	}
}

