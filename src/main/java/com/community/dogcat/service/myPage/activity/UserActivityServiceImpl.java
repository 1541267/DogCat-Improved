package com.community.dogcat.service.myPage.activity;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.community.dogcat.dto.myPage.activity.UserPageRequestDTO;
import com.community.dogcat.dto.myPage.activity.UserPageResponseDTO;
import com.community.dogcat.dto.myPage.activity.UserPostsActivityDTO;
import com.community.dogcat.dto.myPage.activity.UserRepliesActivityDTO;
import com.community.dogcat.dto.myPage.activity.UserScrapsActivityDTO;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.board.reply.ReplyRepository;
import com.community.dogcat.repository.board.scrap.ScrapRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

	private final BoardRepository boardRepository;

	private final ReplyRepository replyRepository;

	private final ScrapRepository scrapRepository;

	// userId에 해당하는 게시물들 목록
	@Override
	public UserPageResponseDTO<UserPostsActivityDTO> listWithPosts(UserPageRequestDTO pageRequestDTO) {

		String[] types = pageRequestDTO.getTypes();
		String keyword = pageRequestDTO.getKeyword();
		Pageable pageable = pageRequestDTO.getPageable();
		String userId = pageRequestDTO.getUserId();

		Page<UserPostsActivityDTO> result = boardRepository.postListWithUser(types, keyword, pageable, userId);

		// 추가할 정보 설정
		List<UserPostsActivityDTO> dtoList = result.getContent().stream()
			.peek(dto -> {
				// 포스트 하나에 달린 댓글 수 설정
				Long postReplyCount = replyRepository.countRepliesByPost(dto.getPostNo());
				dto.setPostReplyCount(postReplyCount != null ? postReplyCount : 0L);
			})
			.collect(Collectors.toList());

		// 회원이 작성한 게시글 수 찾기
		Long postCount = boardRepository.countPostsByUser(userId);
		// 회원이 작성한 댓글 수 찾기
		Long replyCount = replyRepository.countRepliesByUser(userId);
		// 회원이 보관한 보관물 수 찾기
		Long scrapCount = scrapRepository.countScrapsByUser(userId);

		return UserPageResponseDTO.<UserPostsActivityDTO>withAll()
			.pageRequestDTO(pageRequestDTO)
			.dtoList(dtoList)
			.postCount(postCount != null ? postCount : 0L)
			.replyCount(replyCount != null ? replyCount : 0L)
			.scrapCount(scrapCount != null ? scrapCount : 0L)
			.userId(userId)
			.total((int)result.getTotalElements())
			.build();
	}

	// userId에 해당하는 댓글들 목록
	@Override
	public UserPageResponseDTO<UserRepliesActivityDTO> listWithReplies(UserPageRequestDTO pageRequestDTO) {

		String[] types = pageRequestDTO.getTypes();
		String keyword = pageRequestDTO.getKeyword();
		Pageable pageable = pageRequestDTO.getPageable();
		String userId = pageRequestDTO.getUserId();

		Page<UserRepliesActivityDTO> result = replyRepository.repliesListWithUser(types, keyword, pageable, userId);

		// 회원이 작성한 게시글 수 찾기
		Long postCount = boardRepository.countPostsByUser(userId);
		// 회원이 작성한 댓글 수 찾기
		Long replyCount = replyRepository.countRepliesByUser(userId);
		// 회원이 보관한 보관물 수 찾기
		Long scrapCount = scrapRepository.countScrapsByUser(userId);

		return UserPageResponseDTO.<UserRepliesActivityDTO>withAll()
			.pageRequestDTO(pageRequestDTO)
			.dtoList(result.getContent())
			.postCount(postCount != null ? postCount : 0L)
			.replyCount(replyCount != null ? replyCount : 0L)
			.scrapCount(scrapCount != null ? scrapCount : 0L)
			.userId(userId)
			.total((int)result.getTotalElements())
			.build();
	}

	// userId에 해당하는 보관한 게시글 목록
	@Override
	public UserPageResponseDTO<UserScrapsActivityDTO> listWithScraps(UserPageRequestDTO pageRequestDTO) {

		String[] types = pageRequestDTO.getTypes();
		String keyword = pageRequestDTO.getKeyword();
		Pageable pageable = pageRequestDTO.getPageable();
		String userId = pageRequestDTO.getUserId();

		Page<UserScrapsActivityDTO> result = scrapRepository.scrapsListWithUser(types, keyword, pageable, userId);

		// 추가할 정보 설정
		List<UserScrapsActivityDTO> dtoList = result.getContent().stream()
			.peek(dto -> {
				// 포스트 하나에 달린 댓글 수 설정
				Long postReplyCount = replyRepository.countRepliesByPost(dto.getPostNo());
				dto.setPostReplyCount(postReplyCount != null ? postReplyCount : 0L);
			})
			.collect(Collectors.toList());

		// 회원이 작성한 게시글 수 찾기
		Long postCount = boardRepository.countPostsByUser(userId);
		// 회원이 작성한 댓글 수 찾기
		Long replyCount = replyRepository.countRepliesByUser(userId);
		// 회원이 보관한 보관물 수 찾기
		Long scrapCount = scrapRepository.countScrapsByUser(userId);

		return UserPageResponseDTO.<UserScrapsActivityDTO>withAll()
			.pageRequestDTO(pageRequestDTO)
			.dtoList(dtoList)
			.postCount(postCount != null ? postCount : 0L)
			.replyCount(replyCount != null ? replyCount : 0L)
			.scrapCount(scrapCount != null ? scrapCount : 0L)
			.userId(userId)
			.total((int)result.getTotalElements())
			.build();
	}

}

