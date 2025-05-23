package com.community.dogcat.service.board;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.PostLike;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.domain.Scrap;
import com.community.dogcat.domain.User;
import com.community.dogcat.dto.board.BoardListDTO;
import com.community.dogcat.dto.board.BoardPageRequestDTO;
import com.community.dogcat.dto.board.BoardPageResponseDTO;
import com.community.dogcat.dto.board.PostReadDTO;
import com.community.dogcat.dto.board.post.PostDTO;
import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.dto.uploadImage.UploadPostImageResultDTO;
import com.community.dogcat.mapper.UploadResultMappingImgBoard;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.board.postLike.PostLikeRepository;
import com.community.dogcat.repository.board.reply.ReplyRepository;
import com.community.dogcat.repository.board.scrap.ScrapRepository;
import com.community.dogcat.repository.report.ReportLogRepository;
import com.community.dogcat.repository.upload.UploadRepository;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

	// private final S3Uploader s3Uploader;

	private final UserRepository userRepository;

	private final UsersAuthRepository usersAuthRepository;

	private final BoardRepository boardRepository;

	private final ReplyRepository replyRepository;

	private final ScrapRepository scrapRepository;

	private final PostLikeRepository postLikeRepository;

	private final UploadRepository uploadRepository;

	private final ReportLogRepository reportLogRepository;

	// private final UploadImageServiceImpl uploadImageService;

	// private final DeleteTempFiles deleteTempFiles;

	// 업로드된 이미지 정보 얻기 - ys
	private final UploadResultMappingImgBoard uploadResultMappingImgBoard;

	// 게시글 먼저 등록 후
	@Value("${oldUrl}")
	private String oldUrl;

	// 임시 이미지 링크를 s3링크로 변경, 로컬로 전환해서 사용 
	// 로컬용으로 s3에서 uploaded/ 로 변경
	@Value("${newUrl}")
	private String newUrl;

	@Value("${finalUploadPath}")
	private String uploadedPath;

	// 날짜로 파일 경로 분산
	private final String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

	// 수정으로 인한 삭제 이미지 레디스 캐시 갱신(메타, 삭제 둘 다)
	private final RedisTemplate<String, String> rt;

	//게시물을 작성한 회원 정보 조회
	@Override
	public Long register(PostDTO postDTO, List<String> uuids) {

		// 로그인한 회원정보를 받아 userId 조회
		String userId = postDTO.getUserId();
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NoSuchElementException("Board Service Register Error : 401 Unauthorized"));

		// 조회한 회원정보 DTO에 추가
		postDTO.setNickname(user.getNickname());
		postDTO.setExp(user.getExp());
		postDTO.setUserVet(user.isUserVet());

		// 게시글 작성자 경험치 증가
		user.incrementPostExp();

		// 게시글 등록시 이미지가 summernote 링크로 먼저 등록되기 때문에 x 박스가 뜸
		// 고쳐주기 위해 업로드때 수행하던 작업을 게시판 등록할때 적용 - ys
		// s3 사용 x 로컬로 전환
		// if (postDTO.getPostContent().contains(oldUrl)) {
		// 	postDTO.setPostContent(postDTO.getPostContent().replace(oldUrl, newUrl + datePath));
		// }

		// 개선, 폴더 해싱을 위해 추가
		for (String uuid : uuids) {
			String prefix = uuid.substring(0, 2);
			String beforeUrl = oldUrl + uuid;
			String fixedUrl = newUrl
				+ datePath + "/"
				+ prefix + "/"
				+ uuid;

			postDTO.setPostContent(
				postDTO.getPostContent()
					.replaceAll(
						Pattern.quote(beforeUrl),
						Matcher.quoteReplacement(fixedUrl)
					)
			);
		}

		// 게시물 작성
		Post post = Post.builder()
			.userId(user)
			.boardCode(postDTO.getBoardCode())
			.postTitle(postDTO.getPostTitle())
			.postContent(postDTO.getPostContent())
			.regDate(postDTO.getRegDate())
			.postTag(postDTO.getPostTag())
			.secret(postDTO.isSecret())
			.replyAuth(postDTO.isReplyAuth())
			.build();

		boardRepository.save(post);

		return post.getPostNo();
	}

	@Override
	@Transactional
	public void delete(Long postNo, String userId) {

		// 로그인한 회원 정보 확인
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NoSuchElementException("Board Service Delete Error : 401 Unauthorized"));

		// 게시물 번호와 회원 아이디가 일치하는 게시물인지 확인
		Optional<Post> post = boardRepository.findByPostNoAndUserId(postNo, user);

		// 로그인한 회원이 관리자인지 확인
		String auth = usersAuthRepository.findByUserId(userId).getAuthorities();

		// 로그인한 회원이 작성한 게시글이 맞거나 로그인한 계정이 관리자면 삭제 가능
		if (post.isPresent() || auth.equals("ROLE_ADMIN")) {

			// List<ImgBoard> images = uploadRepository.findByPostNo(postNo);
			// deleteTempFiles.deleteUploadedFiles(images);
			// 위의 deleteUploadedFiles 로 변경, 로컬화
			// for (ImgBoard image : images) {
			//
			// 	String fileName = image.getFileUuid() + image.getExtension();
			// 	String thumbFileName = "t_" + fileName;
			//
			// 	deleteTempFiles.deleteFile(thumbFileName);
			// 	deleteTempFiles.deleteFile(fileName);

			// log.info("S3 Delete FileName: {}", fileName);
			// s3Uploader.deleteUploadedS3File(fileName, thumbFileName);

			// 댓글 존재 확인
			List<Reply> replies = replyRepository.findByPostNo(postNo);

			for (Reply reply : replies) {
				// 해당 댓글 신고 삭제
				List<Long> reportLogIds = reportLogRepository.findByReplyNo(reply.getReplyNo());

				for (Long reportLogId : reportLogIds) {
					reportLogRepository.deleteReportLog(reportLogId);
				}
			}
			Set<FileInfoDTO> deletedFiles = uploadRepository.findFileInfoByPostNo(postNo);
			uploadRepository.markFilesDeletedAndUnlinkPost(postNo);

			for (FileInfoDTO df : deletedFiles) {
				rt.opsForHash().delete("imgboard:meta", df.getFullName());
				rt.opsForHash()
					.put("imgboard:toDelete", df.getFullName(), df.getUploadPath() + "|" + df.getUploadThumbPath());
			}
			boardRepository.deleteById(postNo);
		} else {
			log.error("Board Service Delete Error : 403 Forbidden");
		}

	}

	// 게시글 수정용, 수정에 필요한 내용 보기
	@Override
	public PostDTO readOne(Long postNo) {
		// 게시물 번호 조회
		Post post = boardRepository.findById(postNo)
			.orElseThrow(() -> new NoSuchElementException("Board Service ReadOne Error : 404 Not Found"));

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
		Post post = boardRepository.findById(postNo)
			.orElseThrow(() -> new NoSuchElementException("Board Service ReadDetail Error : 404 Not Found"));

		// 로그인한 회원정보를 받아 userId 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NoSuchElementException("Board Service ReadDetail Error : 401 Unauthorized"));

		// 로그인한 회원의 스크랩 여부 확인
		Optional<Scrap> scrap = scrapRepository.findByPostNoAndUserId(post, user);

		// 로그인한 회원의 좋아요/싫어요 여부 확인
		Optional<PostLike> postLike = postLikeRepository.findByPostAndUser(post, user);

		// 게시글 하나에 달린 댓글 수
		Long replyCount = replyRepository.countRepliesByPost(post.getPostNo());

		// 게시물 정보 설정
		PostReadDTO postReadDTO = new PostReadDTO(post);
		postReadDTO.setReplyCount(replyCount);
		postReadDTO.setScrapNo(scrap.map(Scrap::getScrapNo).orElseGet(() -> null));
		postReadDTO.setLikeNo(postLike.map(PostLike::getLikeNo).orElseGet(() -> null));
		postReadDTO.setLikeState(postLike.map(PostLike::isLikeState).orElseGet(() -> false));
		postReadDTO.setDislikeState(postLike.map(PostLike::isDislikeState).orElseGet(() -> false));

		return postReadDTO;
	}

	// 게시글 존재 유무위해 - ys
	@Override
	public Post findPostByPostNo(Long postNo) {

		Optional<Post> optionalPost = boardRepository.findById(postNo);

		return optionalPost.orElseGet(() -> null);
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
		Post post = boardRepository.findById(postDTO.getPostNo())
			.orElseThrow(() -> new NoSuchElementException("Board Service Modify Error : 404 Not Found"));

		// 게시물 작성자 확인
		if (postDTO.getUserId().equals(userId)) {

			// 개선, 로컬 사용시 이미지 제거시 비교후 파일을 제거하기 위해
			// 2025/05/15
			// 수정 전의 이미지 정보들
			List<ImgBoard> imgBoards = post.getImages();

			// 수정된 postContents에서 남은 uuid 추출
			Set<String> uuids = new HashSet<>();
			Pattern p = Pattern.compile("data-uuid\\s*=\\s*\"([^\"]+)\"");
			Matcher m = p.matcher(postDTO.getPostContent());

			while (m.find()) {
				// 위의 정규식으론 수정 전에 업로드 된 이미지는 해싱된 폴더 까지 적혀옴, 분리
				String raw = m.group(1);
				String uuidOnly = raw.contains("/")
					? raw.substring(raw.lastIndexOf('/') + 1)
					: raw;
				uuids.add(uuidOnly);
			}

			List<String> deletedImages = new ArrayList<>();
			List<ImgBoard> deletedImagesMeta = new ArrayList<>();
			if (!imgBoards.isEmpty()) {
				for (ImgBoard img : imgBoards) {
					String uuid = img.getFileUuid();
					if (!uuids.contains(uuid)) {
						deletedImages.add(uuid);
						deletedImagesMeta.add(img);
					}
				}
			}

			// 개선, 수정하면서 제거된 파일은 바로 삭제 했으나 I/O 부담 줄이기 위해
			// mark만 해두고 나중에 스케쥴로 한꺼번에 삭제
			// deleteTempFiles.deleteUploadedFiles(deletedImages);
			uploadRepository.updateAllDeletePossibleTrueAndUnlinkByFileUuid(deletedImages);

			// 삭제된 이미지들은 레디스의 삭제 큐에 삽입, imgboard:delete
			if (!deletedImagesMeta.isEmpty()) {
				for (ImgBoard file : deletedImagesMeta) {
					String uuidKey = file.getFileUuid() + file.getExtension();
					String path = file.getUploadPath().replace(newUrl, uploadedPath);
					String thumbPath = file.getThumbnailPath().replace(newUrl, uploadedPath);

					rt.opsForHash().delete("imgboard:meta", uuidKey);
					rt.opsForHash().put("imgboard:toDelete", uuidKey, path + "|" + thumbPath);
				}
			}

			// s3 사용 x, 로컬로 전환
			// if (postDTO.getPostContent().contains(oldUrl)) {
			// 	postDTO.setPostContent(postDTO.getPostContent().replace(oldUrl, newUrl));
			// }

			// UUID 하나당 한 번만, 임시 경로 → 해시 폴더 경로 포함 최종 경로로 치환
			for (String uuid : uuids) {
				String prefix = uuid.substring(0, 2);       // 해시용 앞 두 글자
				String beforeUrl = oldUrl + uuid;        // "…/temp/7199…"
				String fixedUrl = newUrl
					+ datePath + "/"
					+ prefix + "/"
					+ uuid;

				postDTO.setPostContent(
					postDTO.getPostContent()
						.replaceAll(
							Pattern.quote(beforeUrl),
							Matcher.quoteReplacement(fixedUrl)
						)
				);
			}

			// 수정시간 추가
			postDTO.setModDate(Instant.now());

			// 게시물 수정
			post.modify(postDTO.getBoardCode(), postDTO.getPostTitle(), postDTO.getPostContent(), postDTO.getModDate(),
				postDTO.getPostTag(), postDTO.isSecret(), postDTO.isReplyAuth());

			boardRepository.save(post);

		} else {
			log.error("Board Service Modify Error : 403 Forbidden");
		}

		return post.getPostNo();
	}

	@Override
	public Long completeQna(PostDTO postDTO, String userId) {

		// 게시물 번호 조회
		Post post = boardRepository.findById(postDTO.getPostNo())
			.orElseThrow(() -> new NoSuchElementException("Board Service CompleteQna Error : 404 Not Found"));

		// 게시물 작성자 확인
		if (postDTO.getUserId().equals(userId)) {

			postDTO.setCompleteQna(true);
			// 게시물 수정
			post.completeQna(postDTO.isCompleteQna());

		} else {
			log.error("Board Service completeQna Error : 403 Forbidden");
		}

		return post.getPostNo();
	}

	// regDate(최신순), boardCode에 따라 정렬, 첨부파일 정보 추가
	@Override
	public BoardPageResponseDTO<BoardListDTO> readList(BoardPageRequestDTO pageRequestDTO) {

		// boardCode에 따라 size설정 다르게
		pageRequestDTO.setSizeByBoardCode(pageRequestDTO.getBoardCode());

		Pageable pageable = pageRequestDTO.getPageable();
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
		Pageable pageable = pageRequestDTO.getPageable();
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