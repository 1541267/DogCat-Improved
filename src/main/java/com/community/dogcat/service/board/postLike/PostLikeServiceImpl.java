package com.community.dogcat.service.board.postLike;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.PostLike;
import com.community.dogcat.domain.User;
import com.community.dogcat.dto.board.postLike.PostLikeDTO;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.board.postLike.PostLikeRepository;
import com.community.dogcat.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

	private final UserRepository userRepository;
	private final BoardRepository boardRepository;
	private final PostLikeRepository postLikeRepository;

	@Override
	public Long register(PostLikeDTO postLikeDTO) {

		// 게시물 번호로 게시물 조회
		Post post = boardRepository.findById(postLikeDTO.getPostNo()).orElseThrow(() -> new NoSuchElementException("Post not found"));

		// 로그인한 회원 정보 조회
		String userId = postLikeDTO.getUserId();
		User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

		// 해당 게시물에 대한 사용자의 좋아요, 싫어요 상태 조회
		Optional<PostLike> like = postLikeRepository.findByPostAndUser(post, user);

		//  해당 게시물에 대해 로그인한 유저의 좋아요, 싫어요 상태가 없는 경우 생성
		if (like.isEmpty()) {

			// 좋아요나 싫어요 중 하나만 true로 설정되어야 함
			if (postLikeDTO.isLikeState() && !postLikeDTO.isDislikeState()) {

				// 좋아요 상태 - 싫어요 상태는 false
				PostLike postLike = PostLike.builder()
					.postNo(post)
					.userId(user)
					.likeState(true)
					.dislikeState(false)
					.build();
				// 게시물 좋아요 카운트 수정
				postLikeDTO.setLikeCount(post.getLikeCount() + 1);
				postLikeDTO.setDislikeCount(post.getDislikeCount());
				post.count(
					postLikeDTO.getLikeCount(),
					postLikeDTO.getDislikeCount()
				);

				boardRepository.save(post);
				postLikeRepository.save(postLike);

				return postLike.getLikeNo();

			} else if (postLikeDTO.isDislikeState() && !postLikeDTO.isLikeState()) {

				// 싫어요 상태 - 좋아요 상태는 false
				PostLike postLike = PostLike.builder()
					.postNo(post)
					.userId(user)
					.likeState(false)
					.dislikeState(true)
					.build();
				// 게시물 싫어요 카운트 수정
				postLikeDTO.setLikeCount(post.getLikeCount());
				postLikeDTO.setDislikeCount(post.getDislikeCount() + 1);
				post.count(
					postLikeDTO.getLikeCount(),
					postLikeDTO.getDislikeCount()
				);

				boardRepository.save(post);
				postLikeRepository.save(postLike);

				return postLike.getLikeNo();

			} else {
				return null;  // 좋아요와 싫어요 상태가 동시에 true이거나 둘 다 false인 경우
			}

		} else {
			return null;  // 이미 좋아요나 싫어요를 한 경우
		}
	}

	@Override
	public void delete(Long likeNo, String userId) {

		// 로그인한 회원정보를 받아 userId 조회
		User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

		// likeNo를 통해 postLike 찾아오기
		PostLike postLike = postLikeRepository.findById(likeNo).orElseThrow(() -> new NoSuchElementException("PostLike not found"));

		// 해당 게시물에 대한 사용자의 좋아요, 싫어요 상태 조회
		Optional<PostLike> like = postLikeRepository.findByPostAndUser(postLike.getPostNo(), user);

		// 해당 게시물에 대해 로그인한 유저의 좋아요, 싫어요 상태가 있는 경우 삭제
		if (like.isPresent()) {

			if (postLike.isLikeState() && !postLike.isDislikeState()) {

				Post post = postLike.getPostNo();
				// 게시물 좋아요 카운트 수정
				PostLikeDTO postLikeDTO = PostLikeDTO.builder()
					.likeCount(post.getLikeCount() - 1)
					.dislikeCount(post.getDislikeCount())
					.build();
				post.count(
					postLikeDTO.getLikeCount(),
					postLikeDTO.getDislikeCount()
				);

				boardRepository.save(post);

			} else if (postLike.isDislikeState() && !postLike.isLikeState()) {

				Post post = postLike.getPostNo();
				// 게시물 싫어요 카운트 수정
				PostLikeDTO postLikeDTO = PostLikeDTO.builder()
					.likeCount(post.getLikeCount())
					.dislikeCount(post.getDislikeCount() - 1)
					.build();
				post.count(
					postLikeDTO.getLikeCount(),
					postLikeDTO.getDislikeCount()
				);

				boardRepository.save(post);
			}

			postLikeRepository.deleteById(likeNo);
		}
	}
}


