package com.community.dogcat.repository.search;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.QPost;
import com.community.dogcat.domain.QScrap;
import com.community.dogcat.domain.QUser;
import com.community.dogcat.domain.Scrap;
import com.community.dogcat.dto.myPage.activity.UserScrapsActivityDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;

public class ScrapSearchImpl extends QuerydslRepositorySupport implements ScrapSearch {

	public ScrapSearchImpl() {
		super(Scrap.class);
	}

	// 마이 페이지 : 회원이 보관한 게시글 찾기
	@Override
	public Page<UserScrapsActivityDTO> scrapsListWithUser(String[] types, String keyword, Pageable pageable,
		String userId) {

		// userId에 해당하는 scrap과 scrap한 post 찾는 쿼리 설정
		QUser user = QUser.user;
		QScrap scrap = QScrap.scrap;
		QPost post = QPost.post;
		JPQLQuery<Scrap> query = from(scrap);
		query.join(scrap.userId, user);
		query.join(scrap.postNo, post);
		query.where(user.userId.eq(userId));

		BooleanBuilder booleanBuilder = new BooleanBuilder();
		// 검색 조건
		if ((types != null && types.length > 0) && keyword != null) {
			for (String type : types) {
				switch (type) {
					case "t":
						booleanBuilder.or(post.postTitle.contains(keyword));
						break;
					case "c":
						booleanBuilder.or(post.postContent.contains(keyword));
						break;
				}
			}
		}

		// 사용자 ID 필터
		if (userId != null) {
			booleanBuilder.and(user.userId.eq(userId));
		} else {
			//userId가 없을때는 기본 정렬 기준으로 처리
		}

		// 조건 적용
		query.where(booleanBuilder);
		// 정렬 조건 추가 post regDate 최신순
		query.orderBy(scrap.scrapNo.desc());

		// 페이징 처리
		this.getQuerydsl().applyPagination(pageable, query);

		// 쿼리 실행 및 반환
		List<Scrap> list = query.fetch();

		// UserScrapsActivityDTO List 생성
		List<UserScrapsActivityDTO> dtoList = new ArrayList<>();

		for (Scrap scrap1 : list) {

			UserScrapsActivityDTO userScrapsActivityDTO = UserScrapsActivityDTO.builder()
				.scrapNo(scrap1.getScrapNo())
				.postNo(scrap1.getPostNo().getPostNo())
				.userId(scrap1.getPostNo().getUserId().getUserId())
				.nickname(scrap1.getPostNo().getUserId().getNickname())
				.exp(scrap1.getPostNo().getUserId().getExp())
				.userVet(scrap1.getPostNo().getUserId().isUserVet())
				.boardCode(scrap1.getPostNo().getBoardCode())
				.postTitle(scrap1.getPostNo().getPostTitle())
				.postContent(scrap1.getPostNo().getPostContent())
				.regDate(scrap1.getPostNo().getRegDate())
				.modDate(scrap1.getPostNo().getModDate())
				.postTag(scrap1.getPostNo().getPostTag())
				.secret(scrap1.getPostNo().isSecret())
				.likeCount(scrap1.getPostNo().getLikeCount())
				.dislikeCount(scrap1.getPostNo().getDislikeCount())
				.viewCount(scrap1.getPostNo().getViewCount())
				.replyAuth(scrap1.getPostNo().isReplyAuth())
				.completeQna(scrap1.getPostNo().isCompleteQna())
				.build();

			List<ImgBoard> imgBoards = scrap1.getPostNo().getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.fileName(image.getFileName())
					.uploadTime(image.getUploadTime())
					.build()
				).collect(Collectors.toList());

			userScrapsActivityDTO.setImgBoards(imgBoards);

			// 생성한 userScrapsActivityDTO를 dtoList에 추가
			dtoList.add(userScrapsActivityDTO);
		}

		//전체 갯수
		Long count = query.fetchCount();

		return new PageImpl<>(dtoList, pageable, count);
	}

}