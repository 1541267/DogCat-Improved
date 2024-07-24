package com.community.dogcat.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserReportDetailDTO {

    private String userId;

    private Long postNo;

    private Long replyNo;

    private String replyContent;

    private String postTitle;

    private Long reportNo;

    private Instant regDate;

    private String boardCode;


}