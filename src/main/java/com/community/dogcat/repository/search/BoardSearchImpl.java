package com.community.dogcat.repository.search;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.QPost;
import com.community.dogcat.domain.QReply;
import com.community.dogcat.domain.QUser;
import com.community.dogcat.dto.board.BoardListDTO;
import com.community.dogcat.dto.myPage.activity.UserPostsActivityDTO;
import com.community.dogcat.dto.sample.home.SampleGeneralListDTO;
import com.community.dogcat.dto.sample.home.SampleQnaListDTO;
import com.community.dogcat.dto.sample.home.SampleShowOffListDTO;
import com.community.dogcat.dto.sample.home.SampleTipListDTO;
import com.community.dogcat.dto.sample.home.SampleTodayListDTO;
import com.community.dogcat.dto.sample.search.AllSearchDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BoardSearchImpl extends QuerydslRepositorySupport implements BoardSearch {

	public BoardSearchImpl() {
		super(Post.class);
	}

	// 통합 검색 : 첨부파일 유/무 + 비밀글 제외
	@Override
	public Page<AllSearchDTO> searchAll(String[] types, String keyword, Pageable pageable) {
		QPost post = QPost.post;
		JPQLQuery<Post> query = from(post);
		query.where(post.secret.isFalse());

		BooleanBuilder booleanBuilder = new BooleanBuilder();
		// 검색 조건 유효성 검사
		List<String> validTypes = Arrays.asList("t", "c", "u");
		boolean valid = true;
		// 검색 조건
		if ((types != null && types.length > 0) && keyword != null) {
			for (String type : types) {
				if (!validTypes.contains(type)) {
					valid = false;
					break;
				}
			}

			if (valid) {
				for (String type : types) {
					switch (type) {
						case "t":
							booleanBuilder.or(post.postTitle.contains(keyword));
							break;
						case "c":
							booleanBuilder.or(post.postContent.contains(keyword));
							break;
						case "u":
							booleanBuilder.or(post.userId.nickname.contains(keyword));
							break;
					}
				}
			}
		}

		// 유효하지 않은 조건일 경우 빈 결과 반환
		if (!valid || keyword == null || keyword.isEmpty()) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		// 정렬 조건 추가 regDate 최신순
		query.orderBy(post.regDate.desc());
		// 조건 적용
		query.where(booleanBuilder);

		// 페이징 처리
		this.getQuerydsl().applyPagination(pageable, query);

		// 쿼리 실행 및 반환
		List<Post> list = query.fetch();

		// BoardListDTO List 생성
		List<AllSearchDTO> dtoList = new ArrayList<>();

		for (Post post1 : list) {

			AllSearchDTO allSearchDTO = AllSearchDTO.builder()
				.postNo(post1.getPostNo())
				.userId(post1.getUserId().getUserId())
				.nickname(post1.getUserId().getNickname())
				.exp(post1.getUserId().getExp())
				.userVet(post1.getUserId().isUserVet())
				.boardCode(post1.getBoardCode())
				.postTitle(post1.getPostTitle())
				.postContent(post1.getPostContent())
				.regDate(post1.getRegDate())
				.modDate(post1.getModDate())
				.postTag(post1.getPostTag())
				.secret(post1.isSecret())
				.likeCount(post1.getLikeCount())
				.dislikeCount(post1.getDislikeCount())
				.viewCount(post1.getViewCount())
				.replyAuth(post1.isReplyAuth())
				.build();

			List<ImgBoard> imgBoards = post1.getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.build()
				).collect(Collectors.toList());

			allSearchDTO.setImgBoards(imgBoards);

			// 생성한 boardListDTO를 dtoList에 추가
			dtoList.add(allSearchDTO);
		}
		//전체 갯수
		Long count = query.fetchCount();

		return new PageImpl<>(dtoList, pageable, count);
	}

	// 게시판 : 게시물에 달린 댓글수 + 첨부파일 유/무 + boardCode정렬 (readDetial list에 사용)
	@Override
	public Page<BoardListDTO> listWithBoard(Pageable pageable, String boardCode) {

		// replyCount를 위한 쿼리 설정
		QPost post = QPost.post;
		QReply reply = QReply.reply;
		JPQLQuery<Post> query = from(post);
		query.leftJoin(reply).on(reply.postNo.eq(post));
		query.groupBy(post);

		BooleanBuilder booleanBuilder = new BooleanBuilder();
		// post의 boardCode와 비교
		if (boardCode != null) {
			booleanBuilder.and(post.boardCode.eq(boardCode));
		} else {
			//boardCode가 없을 때는 전체 리스트 출력 됨
		}

		// 조건 추가 postNo > 0
		booleanBuilder.and(post.postNo.gt(0L));
		// 정렬 조건 추가 regDate 최신순
		query.orderBy(post.regDate.desc());

		// 조건 적용
		query.where(booleanBuilder);

		// 페이징 처리
		this.getQuerydsl().applyPagination(pageable, query);

		// SELECT절 설정, countDistinct() 중복을 제거한 카운트 반환
		JPQLQuery<Tuple> tupleQuery = query.select(post, reply.countDistinct());
		// 반환 결과 List<Tuple> 형태로 저장
		List<Tuple> tupleList = tupleQuery.fetch();

		// 각 Tuple을 BoardDTO로 변환
		List<BoardListDTO> dtoList = tupleList.stream().map(tuple -> {

			Post post1 = tuple.get(post);
			Long replyCount = tuple.get(1, Long.class);

			BoardListDTO boardListDTO = BoardListDTO.builder()
				.postNo(post1.getPostNo())
				.userId(post1.getUserId().getUserId())
				.nickname(post1.getUserId().getNickname())
				.exp(post1.getUserId().getExp())
				.userVet(post1.getUserId().isUserVet())
				.boardCode(post1.getBoardCode())
				.postTitle(post1.getPostTitle())
				.postContent(post1.getPostContent())
				.regDate(post1.getRegDate())
				.modDate(post1.getModDate())
				.postTag(post1.getPostTag())
				.secret(post1.isSecret())
				.likeCount(post1.getLikeCount())
				.dislikeCount(post1.getDislikeCount())
				.viewCount(post1.getViewCount())
				.replyAuth(post1.isReplyAuth())
				.replyCount(replyCount)
				.build();

			List<ImgBoard> imgBoards = post1.getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.build()
				).collect(Collectors.toList());

			boardListDTO.setImgBoards(imgBoards);

			return boardListDTO;
		}).collect(Collectors.toList());

		//전체 갯수
		Long count = query.fetchCount();

		return new PageImpl<>(dtoList, pageable, count);
	}

	// 게시판 : 게시물에 달린 댓글수 + 첨부파일 유/무 + boardCode정렬 + 정렬기준선택가능
	@Override
	public Page<BoardListDTO> listWithAll(String[] types, String keyword, Pageable pageable, String boardCode,
		String postTag, String order) {

		// replyCount를 위한 쿼리 설정
		QPost post = QPost.post;
		QReply reply = QReply.reply;
		JPQLQuery<Post> query = from(post);
		query.leftJoin(reply).on(reply.postNo.eq(post));
		query.groupBy(post);

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
					case "u":
						booleanBuilder.or(post.userId.nickname.contains(keyword));
						break;
				}
			}
		}

		// post의 boardCode와 비교
		if (boardCode != null) {
			booleanBuilder.and(post.boardCode.eq(boardCode));
		} else {
			//boardCode가 없을때는 전체 리스트 출력 됨
		}

		// 강아지, 고양이 태그 필터
		if (postTag != null && !postTag.isEmpty()) {
			if ("강아지".equals(postTag)) {
				booleanBuilder.and(post.postTag.eq(postTag));
			} else if ("고양이".equals(postTag)) {
				booleanBuilder.and(post.postTag.eq(postTag));
			} else {
				// 	order가 없을때는 기본 정렬 기준으로 처리
			}
		}

		// 최신순, 댓글 많은 순, 조회수 많은 순 정렬
		if (order != null && !order.isEmpty()) {
			switch (order) {
				case "l":
					query.orderBy(post.regDate.desc());
					break;
				case "r":
					query.orderBy(reply.countDistinct().desc());
					break;
				case "v":
					query.orderBy(post.viewCount.desc());
					break;
			}
		} else {
			//order가 없을때는 기본 정렬 기준으로 처리
		}

		// 정렬 조건 추가 regDate 최신순
		query.orderBy(post.regDate.desc());

		// 검색 시 secret=false 조건 추가
		if (keyword != null && !keyword.isEmpty()) {
			booleanBuilder.and(post.secret.isFalse());
		}

		// 조건 적용
		query.where(booleanBuilder);

		// 페이징 처리
		this.getQuerydsl().applyPagination(pageable, query);

		// SELECT절 설정, countDistinct() 중복을 제거한 카운트 반환
		JPQLQuery<Tuple> tupleQuery = query.select(post, reply.countDistinct());
		// 반환 결과 List<Tuple> 형태로 저장
		List<Tuple> tupleList = tupleQuery.fetch();

		// 각 Tuple을 BoardDTO로 변환
		List<BoardListDTO> dtoList = tupleList.stream().map(tuple -> {

			Post post1 = tuple.get(post);
			Long replyCount = tuple.get(1, Long.class);

			BoardListDTO boardListDTO = BoardListDTO.builder()
				.postNo(post1.getPostNo())
				.userId(post1.getUserId().getUserId())
				.nickname(post1.getUserId().getNickname())
				.exp(post1.getUserId().getExp())
				.userVet(post1.getUserId().isUserVet())
				.boardCode(post1.getBoardCode())
				.postTitle(post1.getPostTitle())
				.postContent(post1.getPostContent())
				.regDate(post1.getRegDate())
				.modDate(post1.getModDate())
				.postTag(post1.getPostTag())
				.secret(post1.isSecret())
				.likeCount(post1.getLikeCount())
				.dislikeCount(post1.getDislikeCount())
				.viewCount(post1.getViewCount())
				.replyAuth(post1.isReplyAuth())
				.replyCount(replyCount)
				.build();

			List<ImgBoard> imgBoards = post1.getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.thumbnailPath(image.getThumbnailPath())
					.build()
				).collect(Collectors.toList());

			boardListDTO.setImgBoards(imgBoards);

			return boardListDTO;
		}).collect(Collectors.toList());

		//전체 갯수
		Long count = query.fetchCount();

		return new PageImpl<>(dtoList, pageable, count);
	}

	// 마이 페이지 : 회원이 작성한 게시물 검색 및 페이징
	@Override
	public Page<UserPostsActivityDTO> postListWithUser(String[] types, String keyword, Pageable pageable,
		String userId) {

		// userId에 해당하는 post 찾는 쿼리 설정
		QUser user = QUser.user;
		QPost post = QPost.post;
		JPQLQuery<Post> query = from(post);
		query.join(post.userId, user);
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
			//userId가 없을때는 기본 정렬 기준으로 처리 됨
		}

		// 정렬 조건 추가 regDate 최신순
		query.orderBy(post.regDate.desc());
		// 조건 적용
		query.where(booleanBuilder);

		// 페이징 처리
		this.getQuerydsl().applyPagination(pageable, query);

		// 쿼리 실행 및 반환
		List<Post> list = query.fetch();

		// UserPostsActivityDTO List 생성
		List<UserPostsActivityDTO> dtoList = new ArrayList<>();

		for (Post post1 : list) {

			UserPostsActivityDTO userPostsActivityDTO = UserPostsActivityDTO.builder()
				.postNo(post1.getPostNo())
				.userId(post1.getUserId().getUserId())
				.nickname(post1.getUserId().getNickname())
				.exp(post1.getUserId().getExp())
				.userVet(post1.getUserId().isUserVet())
				.boardCode(post1.getBoardCode())
				.postTitle(post1.getPostTitle())
				.postContent(post1.getPostContent())
				.regDate(post1.getRegDate())
				.modDate(post1.getModDate())
				.postTag(post1.getPostTag())
				.secret(post1.isSecret())
				.likeCount(post1.getLikeCount())
				.dislikeCount(post1.getDislikeCount())
				.viewCount(post1.getViewCount())
				.replyAuth(post1.isReplyAuth())
				.build();

			List<ImgBoard> imgBoards = post1.getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.build()
				).collect(Collectors.toList());

			userPostsActivityDTO.setImgBoards(imgBoards);

			// 생성한 userPostsActivityDTO를 dtoList에 추가
			dtoList.add(userPostsActivityDTO);
		}

		//전체 갯수
		Long count = query.fetchCount();

		return new PageImpl<>(dtoList, pageable, count);
	}

	// 홈 : 실시간 인기 게시글 리스트
	@Override
	public List<SampleTodayListDTO> todayList(Instant startOfDay, Instant endOfDay, int size) {

		QPost post = QPost.post;
		JPQLQuery<Post> query = from(post);
		query.where(post.regDate.between(startOfDay, endOfDay).and(post.secret.isFalse()));
		query.orderBy(post.viewCount.desc());
		query.limit(size);

		// 쿼리 실행 및 반환
		List<Post> list = query.fetch();

		// SampleTodayListDTO List 생성
		List<SampleTodayListDTO> dtoList = new ArrayList<>();

		for (Post post1 : list) {

			SampleTodayListDTO sampleTodayListDTO = SampleTodayListDTO.builder()
				.postNo(post1.getPostNo())
				.userId(post1.getUserId().getUserId())
				.nickname(post1.getUserId().getNickname())
				.exp(post1.getUserId().getExp())
				.userVet(post1.getUserId().isUserVet())
				.boardCode(post1.getBoardCode())
				.postTitle(post1.getPostTitle())
				.postContent(post1.getPostContent())
				.regDate(post1.getRegDate())
				.modDate(post1.getModDate())
				.postTag(post1.getPostTag())
				.secret(post1.isSecret())
				.likeCount(post1.getLikeCount())
				.dislikeCount(post1.getDislikeCount())
				.viewCount(post1.getViewCount())
				.replyAuth(post1.isReplyAuth())
				.build();

			List<ImgBoard> imgBoards = post1.getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.build()
				).collect(Collectors.toList());

			sampleTodayListDTO.setImgBoards(imgBoards);

			// 생성한 sampleTodayListDTO를 dtoList에 추가
			dtoList.add(sampleTodayListDTO);
		}

		return dtoList;
	}

	// 홈 : 게시판별 리스트 showOff, 첨부파일 유/무 + 비밀글 제외
	@Override
	public List<SampleShowOffListDTO> showOffList(int size) {
		QPost post = QPost.post;
		JPQLQuery<Post> query = from(post);
		query.where(post.secret.isFalse().and(post.boardCode.eq("showOff")));
		query.orderBy(post.regDate.desc());
		query.limit(size);

		// 쿼리 실행 및 반환
		List<Post> list = query.fetch();

		// SampleSowOffListDTO List 생성
		List<SampleShowOffListDTO> dtoList = new ArrayList<>();

		for (Post post1 : list) {

			SampleShowOffListDTO sampleShowOffListDTO = SampleShowOffListDTO.builder()
				.postNo(post1.getPostNo())
				.userId(post1.getUserId().getUserId())
				.nickname(post1.getUserId().getNickname())
				.exp(post1.getUserId().getExp())
				.userVet(post1.getUserId().isUserVet())
				.boardCode(post1.getBoardCode())
				.postTitle(post1.getPostTitle())
				.postContent(post1.getPostContent())
				.regDate(post1.getRegDate())
				.modDate(post1.getModDate())
				.postTag(post1.getPostTag())
				.secret(post1.isSecret())
				.likeCount(post1.getLikeCount())
				.dislikeCount(post1.getDislikeCount())
				.viewCount(post1.getViewCount())
				.replyAuth(post1.isReplyAuth())
				.build();

			List<ImgBoard> imgBoards = post1.getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.thumbnailPath(image.getThumbnailPath())
					.build()
				).collect(Collectors.toList());

			sampleShowOffListDTO.setImgBoards(imgBoards);

			// 생성한 sampleShowOffListDTO를 dtoList에 추가
			dtoList.add(sampleShowOffListDTO);
		}

		return dtoList;
	}

	// 홈 : 게시판별 리스트 general, 첨부파일 유/무 + 비밀글 제외
	@Override
	public List<SampleGeneralListDTO> generalList(int size) {
		QPost post = QPost.post;
		JPQLQuery<Post> query = from(post);
		query.where(post.secret.isFalse().and(post.boardCode.eq("general")));
		query.orderBy(post.regDate.desc());
		query.limit(size);

		// 쿼리 실행 및 반환
		List<Post> list = query.fetch();

		// SampleGeneralListDTO List 생성
		List<SampleGeneralListDTO> dtoList = new ArrayList<>();

		for (Post post1 : list) {

			SampleGeneralListDTO sampleGeneralListDTO = SampleGeneralListDTO.builder()
				.postNo(post1.getPostNo())
				.userId(post1.getUserId().getUserId())
				.nickname(post1.getUserId().getNickname())
				.exp(post1.getUserId().getExp())
				.userVet(post1.getUserId().isUserVet())
				.boardCode(post1.getBoardCode())
				.postTitle(post1.getPostTitle())
				.postContent(post1.getPostContent())
				.regDate(post1.getRegDate())
				.modDate(post1.getModDate())
				.postTag(post1.getPostTag())
				.secret(post1.isSecret())
				.likeCount(post1.getLikeCount())
				.dislikeCount(post1.getDislikeCount())
				.viewCount(post1.getViewCount())
				.replyAuth(post1.isReplyAuth())
				.build();

			List<ImgBoard> imgBoards = post1.getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.build()
				).collect(Collectors.toList());

			sampleGeneralListDTO.setImgBoards(imgBoards);

			// 생성한 sampleGeneralListDTO를 dtoList에 추가
			dtoList.add(sampleGeneralListDTO);
		}

		return dtoList;
	}

	// 홈 : 게시판별 리스트 tip, 첨부파일 유/무 + 비밀글 제외
	@Override
	public List<SampleTipListDTO> tipList(int size) {
		QPost post = QPost.post;
		JPQLQuery<Post> query = from(post);
		query.where(post.secret.isFalse().and(post.boardCode.eq("tip")));
		query.orderBy(post.regDate.desc());
		query.limit(size);

		// 쿼리 실행 및 반환
		List<Post> list = query.fetch();

		// SampleTipListDTO List 생성
		List<SampleTipListDTO> dtoList = new ArrayList<>();

		for (Post post1 : list) {

			SampleTipListDTO sampleTipListDTO = SampleTipListDTO.builder()
				.postNo(post1.getPostNo())
				.userId(post1.getUserId().getUserId())
				.nickname(post1.getUserId().getNickname())
				.exp(post1.getUserId().getExp())
				.userVet(post1.getUserId().isUserVet())
				.boardCode(post1.getBoardCode())
				.postTitle(post1.getPostTitle())
				.postContent(post1.getPostContent())
				.regDate(post1.getRegDate())
				.modDate(post1.getModDate())
				.postTag(post1.getPostTag())
				.secret(post1.isSecret())
				.likeCount(post1.getLikeCount())
				.dislikeCount(post1.getDislikeCount())
				.viewCount(post1.getViewCount())
				.replyAuth(post1.isReplyAuth())
				.build();

			List<ImgBoard> imgBoards = post1.getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.build()
				).collect(Collectors.toList());

			sampleTipListDTO.setImgBoards(imgBoards);

			// 생성한 sampleTipListDTO를 dtoList에 추가
			dtoList.add(sampleTipListDTO);
		}

		return dtoList;
	}

	// 홈 : 게시판별 리스트 qna, 첨부파일 유/무 + 비밀글 제외
	@Override
	public List<SampleQnaListDTO> qnaList(int size) {
		QPost post = QPost.post;
		JPQLQuery<Post> query = from(post);
		query.where(post.secret.isFalse().and(post.boardCode.eq("qna")));
		query.orderBy(post.regDate.desc());
		query.limit(size);

		// 쿼리 실행 및 반환
		List<Post> list = query.fetch();

		// SampleQnaListDTO List 생성
		List<SampleQnaListDTO> dtoList = new ArrayList<>();

		for (Post post1 : list) {

			SampleQnaListDTO sampleQnaListDTO = SampleQnaListDTO.builder()
				.postNo(post1.getPostNo())
				.userId(post1.getUserId().getUserId())
				.nickname(post1.getUserId().getNickname())
				.exp(post1.getUserId().getExp())
				.userVet(post1.getUserId().isUserVet())
				.boardCode(post1.getBoardCode())
				.postTitle(post1.getPostTitle())
				.postContent(post1.getPostContent())
				.regDate(post1.getRegDate())
				.modDate(post1.getModDate())
				.postTag(post1.getPostTag())
				.secret(post1.isSecret())
				.likeCount(post1.getLikeCount())
				.dislikeCount(post1.getDislikeCount())
				.viewCount(post1.getViewCount())
				.replyAuth(post1.isReplyAuth())
				.build();

			List<ImgBoard> imgBoards = post1.getImages().stream()
				.map(image -> ImgBoard.builder()
					.fileUuid(image.getFileUuid())
					.build()
				).collect(Collectors.toList());

			sampleQnaListDTO.setImgBoards(imgBoards);

			// 생성한 sampleQnaListDTO를 dtoList에 추가
			dtoList.add(sampleQnaListDTO);
		}

		return dtoList;
	}
}