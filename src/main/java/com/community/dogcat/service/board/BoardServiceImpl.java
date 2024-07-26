package com.community.dogcat.service.board;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.PostLike;
import com.community.dogcat.domain.Scrap;
import com.community.dogcat.domain.User;
import com.community.dogcat.domain.UsersAuth;
import com.community.dogcat.dto.board.BoardListDTO;
import com.community.dogcat.dto.board.BoardPageRequestDTO;
import com.community.dogcat.dto.board.BoardPageResponseDTO;
import com.community.dogcat.dto.board.PostReadDTO;
import com.community.dogcat.dto.board.post.PostDTO;
import com.community.dogcat.dto.uploadImage.UploadPostImageResultDTO;
import com.community.dogcat.mapper.UploadResultMappingImgBoard;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.board.postLike.PostLikeRepository;
import com.community.dogcat.repository.board.reply.ReplyRepository;
import com.community.dogcat.repository.board.scrap.ScrapRepository;
import com.community.dogcat.repository.upload.UploadRepository;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;
import com.community.dogcat.util.uploader.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

	private final S3Uploader s3Uploader;

	private final UserRepository userRepository;

	private final UsersAuthRepository usersAuthRepository;

	private final BoardRepository boardRepository;

	private final ReplyRepository replyRepository;

	private final ScrapRepository scrapRepository;

	private final PostLikeRepository postLikeRepository;

	private final UploadRepository uploadRepository;

	// 업로드된 이미지 정보 얻기 - ys
	private final UploadResultMappingImgBoard uploadResultMappingImgBoard;

	// 게시글 먼저 등록 후
	@Value("${oldUrl}")
	private String oldUrl;

	@Value("${newUrl}")
	private String newUrl;

	//게시물을 작성한 회원 정보 조회
	@Override
	public Long register(PostDTO postDTO) {
		// 로그인한 회원정보를 받아 userId 조회
		String userId = postDTO.getUserId();
		User user = userRepository.findById(userId).orElseThrow();
		log.info(user);

		// 조회한 회원정보 DTO에 추가
		postDTO.setNickname(user.getNickname());
		postDTO.setExp(user.getExp());
		postDTO.setUserVet(user.isUserVet());
		log.info(postDTO.getNickname());
		log.info(postDTO.getExp());
		log.info(postDTO.isUserVet());

		// 게시글 작성자 경험치 증가
		user.incrementPostExp();

		// 게시글 등록시 이미지가 summernote 링크로 먼저 등록되기 때문에 x 박스가 뜸
		// 고쳐주기 위해 업로드때 수행하던 작업을 게시판 등록할때 적용 - ys
		if (postDTO.getPostContent().contains(oldUrl)) {
			postDTO.setPostContent(postDTO.getPostContent().replace(oldUrl, newUrl));
		}
		// 게시물 작성
		Post post = Post.builder()
			.userId(user)
			.boardCode(postDTO.getBoardCode())
			.modDate(postDTO.getModDate())
			.dislikeCount(postDTO.getDislikeCount())
			.postContent(postDTO.getPostContent())
			.postTag(postDTO.getPostTag())
			.postTitle(postDTO.getPostTitle())
			.likeCount(postDTO.getLikeCount())
			.regDate(postDTO.getRegDate())
			.replyAuth(postDTO.isReplyAuth())
			.secret(postDTO.isSecret())
			.viewCount(postDTO.getViewCount())
			.build();

		boardRepository.save(post);

		return post.getPostNo();
	}

	@Override
	@Transactional
	public void delete(Long postNo, String userId) {
		// 로그인한 회원 정보 확인
		User user = userRepository.findById(userId).orElseThrow();

		// 게시물 번호와 회원 아이디가 일치하는 게시물인지 확인
		Optional<Post> post = boardRepository.findByPostNoAndUserId(postNo, user);

		// 로그인한 회원이 관리자인지 확인
		String auth = usersAuthRepository.findByUserId(userId).getAuthorities();
		log.info(auth);

		// 로그인한 회원이 작성한 게시글이 맞거나 로그인한 계정이 관리자면 삭제 가능
		if (post.isPresent() || auth.equals("ROLE_ADMIN")) {
			List<ImgBoard> images = uploadRepository.findByPostNo(postNo);
			for (ImgBoard image : images) {
				String fileName = image.getFileUuid() + image.getExtension();
				String thumbFileName = "t_" + fileName;
				log.info("S3 Delete FileName: {}", fileName);

				s3Uploader.removeS3File(fileName, thumbFileName);
			}
			boardRepository.deleteById(postNo);
		}
	}

	// 게시글 수정용, 수정에 필요한 내용 보기
	@Override
	public PostDTO readOne(Long postNo) {
		// 게시물 번호 조회
		Post post = boardRepository.findById(postNo).orElseThrow();
		return new PostDTO(post);
	}

	// 상세페이지 접속시 조회수 증가
	@Override
	public void updateViewCount(Long postNo) {
		boardRepository.updateViewCount(postNo);
	}

	// 게시글 상세보기
	@Override
	@Transactional
	public PostReadDTO readDetail(Long postNo, String userId) {
		// 게시물 번호 조회 (게시물 정보 확인용)
		Post post = boardRepository.findById(postNo).orElseThrow();

		// 로그인한 회원정보를 받아 userId 조회
		User user = userRepository.findById(userId).orElseThrow();

		// 로그인한 회원의 스크랩 여부 확인
		Optional<Scrap> scrap = scrapRepository.findByPostNoAndUserId(post, user);

		// 로그인한 회원의 좋아요/싫어요 여부 확인
		Optional<PostLike> postLike = postLikeRepository.findByPostAndUser(post, user);

		// 게시물 정보 설정
		return PostReadDTO.builder()
			.postNo(post.getPostNo())
			.userId(post.getUserId().getUserId())
			.nickname(post.getUserId().getNickname())
			.exp(post.getUserId().getExp())
			.userVet(post.getUserId().isUserVet())
			.boardCode(post.getBoardCode())
			.postTitle(post.getPostTitle())
			.postContent(post.getPostContent())
			.regDate(post.getRegDate())
			.modDate(post.getModDate())
			.postTag(post.getPostTag())
			.secret(post.isSecret())
			.likeCount(post.getLikeCount())
			.dislikeCount(post.getDislikeCount())
			.viewCount(post.getViewCount())
			.replyAuth(post.isReplyAuth())
			.replyCount(replyRepository.countRepliesByPostNo(post))
			.scrapNo(scrap.map(Scrap::getScrapNo).orElseGet(() -> null))
			.likeNo(postLike.map(PostLike::getLikeNo).orElseGet(() -> null))
			.likeState(postLike.map(PostLike::isLikeState).orElseGet(() -> false))
			.dislikeState(postLike.map(PostLike::isDislikeState).orElseGet(() -> false))
			.imgBoards(post.getImages().stream().toList())
			.build();
	}

	// 게시판 존재 유무위해 - ys
	@Override
	public Post findPostByPostNo(Long postNo) {

		Optional<Post> optionalPost = boardRepository.findById(postNo);

		return optionalPost.get();
	}

	// 게시글 조회시 사진 찾기 - ys
	@Override
	public List<String> getImages(Long postNo) {

		Optional<Post> optionalPost = boardRepository.findById(postNo);
		if (optionalPost.isPresent()) {

			//Optional로 보내면 view에서 th:each 사용 불가 - ys
			Post post = optionalPost.get();

			List<ImgBoard> imgBoards = post.getImages();

			//맵퍼를 사용해 엔티티를 DTO 객체로 변환
			List<UploadPostImageResultDTO> images = imgBoards.stream()
				.map(uploadResultMappingImgBoard::convertToDTO)
				.toList();

			List<String> uploadPaths = new ArrayList<>();

			for (UploadPostImageResultDTO uploadPath : images) {
				uploadPaths.add(uploadPath.getUploadPath());
			}
			return uploadPaths;
		}
		return null;
	}

	@Override
	public Long modify(PostDTO postDTO, String userId) {
		// 게시물 번호 조회
		Post post = boardRepository.findById(postDTO.getPostNo()).orElseThrow();
		log.info("modify / postNo: {}, postContent: {}", post.getPostNo(), post.getPostContent());

		// 게시물 작성자 확인
		if (postDTO.getUserId().equals(userId)) {

			if (postDTO.getPostContent().contains(oldUrl)) {
				postDTO.setPostContent(postDTO.getPostContent().replace(oldUrl, newUrl));
			}

			// 수정시간 추가
			postDTO.setModDate(Instant.now());

			// 게시물 수정
			post.modify(
				postDTO.getBoardCode(),
				postDTO.getPostTitle(),
				postDTO.getPostContent(),
				postDTO.getModDate(),
				postDTO.getPostTag(),
				postDTO.isSecret(),
				postDTO.isReplyAuth());

			boardRepository.save(post);
		}
		return post.getPostNo();
	}

	// regDate(최신순), boarCode에 따라 정렬, 첨부파일 정보 추가
	@Override
	public BoardPageResponseDTO<BoardListDTO> readList(BoardPageRequestDTO pageRequestDTO) {
		Pageable pageable = pageRequestDTO.getPageable("postNo");
		String boardCode = pageRequestDTO.getBoardCode();

		Page<BoardListDTO> result = boardRepository.listWithBoard(pageable, boardCode);

		return BoardPageResponseDTO.<BoardListDTO>withAll()
			.pageRequestDTO(pageRequestDTO)
			.dtoList(result.getContent())
			.total((int)result.getTotalElements())
			.build();
	}

	// regDate(최신순), boarCode에 따라 정렬, 첨부파일 정보 추가 + 정렬기준선택가능
	@Override
	public BoardPageResponseDTO<BoardListDTO> list(BoardPageRequestDTO pageRequestDTO) {
		// boardCode에 따라 size설정 다르게
		pageRequestDTO.setSizeByBoardCode(pageRequestDTO.getBoardCode());

		String[] types = pageRequestDTO.getTypes();
		String keyword = pageRequestDTO.getKeyword();
		Pageable pageable = pageRequestDTO.getPageable("postNo");
		String boardCode = pageRequestDTO.getBoardCode();
		String postTag = pageRequestDTO.getPostTag();
		String order = pageRequestDTO.getOrder();

		Page<BoardListDTO> result = boardRepository.listWithAll(types, keyword, pageable, boardCode, postTag, order);

		return BoardPageResponseDTO.<BoardListDTO>withAll()
			.pageRequestDTO(pageRequestDTO)
			.dtoList(result.getContent())
			.total((int)result.getTotalElements())
			.build();
	}

}

