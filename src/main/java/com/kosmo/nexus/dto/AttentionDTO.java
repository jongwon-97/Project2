package com.kosmo.nexus.dto;

import lombok.*;

import java.sql.Date;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class AttentionDTO {
    private int attentionId;                // 리뷰 ID
    private String memberId;                // 사용자 ID
    private Integer companyId;              // 회사 고유 번호
    private int seasonId;                   // 회차 ID

    private String attentionStatus;         // 상태 (참여/미참여)
    private Date attentionApplDate;         // 신청일

    private int reviewRating;               // 별점
    private String reviewContent;           // 리뷰
    private Date reviewDate;                // 작성일

}
