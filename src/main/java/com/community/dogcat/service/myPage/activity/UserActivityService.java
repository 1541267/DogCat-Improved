package com.community.dogcat.service.myPage.activity;

import com.community.dogcat.dto.myPage.activity.UserPageRequestDTO;
import com.community.dogcat.dto.myPage.activity.UserPageResponseDTO;
import com.community.dogcat.dto.myPage.activity.UserPostsActivityDTO;
import com.community.dogcat.dto.myPage.activity.UserRepliesActivityDTO;
import com.community.dogcat.dto.myPage.activity.UserScrapsActivityDTO;

public interface UserActivityService {

	// userId에 해당하는 게시물들 목록
	UserPageResponseDTO<UserPostsActivityDTO> listWithPosts(UserPageRequestDTO pageRequestDTO);

	// userId에 해당하는 댓글들 목록
	UserPageResponseDTO<UserRepliesActivityDTO> listWithReplies(UserPageRequestDTO pageRequestDTO);

	// userId에 해당하는 보관한 게시글 목록
	UserPageResponseDTO<UserScrapsActivityDTO> listWithScraps(UserPageRequestDTO pageRequestDTO);

}
