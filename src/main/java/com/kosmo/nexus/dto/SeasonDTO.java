package com.kosmo.nexus.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class SeasonDTO {

    private int seasonId; //시즌 ID
    private String seasonTitle; //시즌별 행사 제목
    private String seasonInfo; //시즌별 행사 정보
    private int seasonLimit; //제한 인원
    private int seasonFee; //참가비
    private int seasonViews; //조회수
    private String seasonStartDate; //모집 시작일
    private String seasonEndDate; //모집 종료일
    private int eventId; //행사 ID
    private String seasonState; //시즌
    private int boardId;
    private int roundNumber; //회차 값(N회차)

    private String seasonThumbnail; // 썸네일 경로
}
