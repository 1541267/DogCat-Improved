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
public class ReportLogDTO {

    private Long reportNo;

    private String userId;

    private String nickname;

    private Long postNo;

    private Long replyNo;

    private String reportTitle;

    private String reportContent;

    private Instant regDate;

}
