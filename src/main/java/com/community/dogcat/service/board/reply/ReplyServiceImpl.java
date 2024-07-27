package com.community.dogcat.service.board.reply;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.community.dogcat.repository.report.ReportLogRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.domain.User;
import com.community.dogcat.dto.board.reply.ReplyDTO;
import com.community.dogcat.repository.board.BoardRepository;
import com.community.dogcat.repository.board.reply.ReplyRepository;
import com.community.dogcat.repository.user.UserRepository;
import com.community.dogcat.repository.user.UsersAuthRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {

	private final UserRepository userRepository;
	private final UsersAuthRepository usersAuthRepository;
	private final BoardRepository boardRepository;
	private final ReplyRepository replyRepository;
	private final ReportLogRepository reportLogRepository;

	private final ModelMapper modelMapper;

	@Override
	public Long register(ReplyDTO replyDTO) {

		// 로그인한 회원정보를 받아 userId 조회
		String userId = replyDTO.getUserId();
		User user = userRepository.findById(userId).orElseThrow();
		// 조회한 회원정보 DTO에 추가
		replyDTO.setNickname(user.getNickname());

		// 댓글 작성을 위해 게시물 번호 조회
		Post post = boardRepository.findById(replyDTO.getPostNo()).orElseThrow();

		// 게시물이 비밀글인 경우
		boolean secret = post.isSecret();
		// 댓글 권한이 수의사만 가능한 경우
		boolean replyAuth = post.isReplyAuth();
		// 관리자거나, 수의사거나
		String auth = usersAuthRepository.findByUserId(userId).getAuthorities();
		// 로그인한 유저가 게시글 작성자일때
		Optional<Post> postUser = boardRepository.findByPostNoAndUserId(post.getPostNo(), user);

		if (secret || replyAuth) {
			if (postUser.isPresent() || auth.equals("ROLE_ADMIN") ||  auth.equals("ROLE_VET")) {
				// 댓글 작성
				Reply reply = Reply.builder()
					.replyContent(replyDTO.getReplyContent())
					.regDate(replyDTO.getRegDate())
					.userId(user)
					.postNo(post)
					.build();
				replyRepository.save(reply);
			}
		} else {
		// 비밀글이 아닐때 누구나 댓글 작성 가능
		Reply reply = Reply.builder()
			.replyContent(replyDTO.getReplyContent())
			.regDate(replyDTO.getRegDate())
			.userId(user)
			.postNo(post)
			.build();
		replyRepository.save(reply);
		}

		return replyDTO.getReplyNo();
	}

	@Override
	public ReplyDTO read(Long replyNo) {

		Optional<Reply> replyOptional = replyRepository.findById(replyNo);

		Reply reply = replyOptional.orElseThrow();

		return modelMapper.map(reply, ReplyDTO.class);
	}

	@Override
	public void delete(Long replyNo, String userId) {
		// 로그인한 회원 정보 확인
		User user = userRepository.findById(userId).orElseThrow();
		// 댓글 번호와 회원 아이디가 일치하는 댓글인지 확인
		Optional<Reply> reply = replyRepository.findByReplyNoAndUserId(replyNo, user);

		// 로그인한 회원이 관리자인지 확인
		String auth = usersAuthRepository.findByUserId(userId).getAuthorities();
		log.info(auth);

		// 회원 아이디로 작성한 댓글이면 삭제
		if (reply.isPresent()|| auth.equals("ROLE_ADMIN")) {
			replyRepository.deleteById(replyNo);

			// 해당 댓글 신고 삭제
			List<Long> reportLogIds = reportLogRepository.findByReplyNo(replyNo);
			for (Long reportLogId : reportLogIds) {
				reportLogRepository.deleteReportLog(reportLogId);
			}


		}
	}

	@Override
	public List<ReplyDTO> getListOfReply(Long postNo) {
		Post post = boardRepository.findById(postNo).orElseThrow();
		// Post post = Post.builder().postNo(postNo).build();
		List<Reply> replies = replyRepository.findByPostNo(post);

		return replies.stream()
			.map(reply -> new ReplyDTO(reply)).collect(Collectors.toList());
	}
//	@Override
	public Reply findReplyByReplyNo(Long replyNo) {
		Optional<Reply> optionalReply = replyRepository.findById(replyNo);
		Reply reply = optionalReply.get();
		return reply;
	}
}
