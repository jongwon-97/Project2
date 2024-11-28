package com.kosmo.nexus.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class NoticeDTO {
    private int boardId; // 게시글 고유 ID (식별자)
    private String boardTitle; // 게시글 제목
    private String boardContent; // 게시글 내용
    private int boardViews; // 게시글 조회수
    private String boardCreateDate; // 게시글 작성 날짜
    private String boardCategory; // 게시글 카테고리 (예: 공지사항, Q&A 등)
    private String memberId = "defaultUser"; // 게시글 작성자 ID
    private Boolean isNotice; // 공지 여부

}
