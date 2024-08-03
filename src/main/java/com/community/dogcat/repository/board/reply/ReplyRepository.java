package com.community.dogcat.repository.board.reply;

import com.community.dogcat.domain.Post;
import com.community.dogcat.domain.Reply;
import com.community.dogcat.domain.User;
import com.community.dogcat.repository.search.ReplySearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long>, ReplySearch {

    //게시물 하나에 달린 댓글 목록
    @Query("SELECT r FROM Reply r WHERE r.postNo.postNo = :postNo")
    List<Reply> findByPostNo(@Param("postNo") Long postNo);

    // 게시물 하나에 달린 댓글 갯수 카운트
    @Query("SELECT count(r) FROM Reply r WHERE r.postNo.postNo = :postNo")
    Long countRepliesByPost(@Param("postNo") Long postNo);

    // userId 와 replyNo 가 일치하는 댓글 찾기
    @Query("SELECT r FROM Reply r WHERE r.replyNo = :replyNo AND r.userId = :userId")
    Optional<Reply> findByReplyNoAndUserId(@Param("replyNo") Long replyNo, @Param("userId") User userId);

    // userId에 해당하는 reply를 찾아서 목록에 담기
    @Query("SELECT r FROM Reply r WHERE r.userId = :userId")
    List<Reply> findByUserId(@Param("userId") User userId);

    // userId에 해당하는 댓글 갯수 카운트(마이페이지용)
    @Query("SELECT count(r) FROM Reply r WHERE r.userId.userId = :userId")
    Long countRepliesByUser(@Param("userId") String userId);

    Reply findByReplyNo(Long replyNo);

    @Transactional
    void deleteAllByUserId(User user);

    List<Reply> findAllByUserId(User user);
}
