package com.community.dogcat.repository.search;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.community.dogcat.domain.QReply;
import com.community.dogcat.domain.QUser;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.dto.myPage.activity.UserRepliesActivityDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;

public class ReplySearchImpl extends QuerydslRepositorySupport implements ReplySearch {

	public ReplySearchImpl() {
		super(Reply.class);
	}

	// 마이 페이지 : 회원이 작성한 댓글 찾기
	@Override
	public Page<UserRepliesActivityDTO> repliesListWithUser(String[] types, String keyword, Pageable pageable,
		String userId) {

		// userId에 해당하는 reply 찾는 쿼리 설정
		QUser user = QUser.user;
		QReply reply = QReply.reply;
		JPQLQuery<Reply> query = from(reply);
		query.join(reply.userId, user);
		query.where(user.userId.eq(userId));

		BooleanBuilder booleanBuilder = new BooleanBuilder();
		// 검색 조건
		if (keyword != null) {
			booleanBuilder.or(reply.replyContent.contains(keyword));
		}

		// 사용자 ID 필터
		if (userId != null) {
			booleanBuilder.and(user.userId.eq(userId));
		} else {
			//userId가 없을때는 기본 정렬 기준으로 처리 됨
		}

		// 조건 적용
		query.where(booleanBuilder);
		// 정렬 조건 추가 post regDate 최신순
		query.orderBy(reply.regDate.desc());

		// 페이징 처리
		this.getQuerydsl().applyPagination(pageable, query);

		// 쿼리 실행 및 반환
		List<Reply> list = query.fetch();

		// UserRepliesActivityDTO List 생성
		List<UserRepliesActivityDTO> dtoList = new ArrayList<>();

		for (Reply reply1 : list) {

			UserRepliesActivityDTO userRepliesActivityDTO = UserRepliesActivityDTO.builder()
				.replyNo(reply1.getReplyNo())
				.userId(reply1.getUserId().getUserId())
				.nickname(reply1.getUserId().getNickname())
				.postNo(reply1.getPostNo().getPostNo())
				.boardCode(reply1.getPostNo().getBoardCode())
				.replyContent(reply1.getReplyContent())
				.regDate(reply1.getRegDate())
				.build();

			// 생성한 userRepliesActivityDTO를 dtoList에 추가
			dtoList.add(userRepliesActivityDTO);
		}

		//전체 갯수
		Long count = query.fetchCount();

		return new PageImpl<>(dtoList, pageable, count);
	}

}