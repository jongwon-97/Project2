package com.kosmo.nexus.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class FileDTO {
    private int fileId;             // 파일 ID
    private String filePath;        // 정적 리소스 경로
    private int boardId;            // 게시글 ID
    private long fileSize;          // 파일 크기
    private String fileDate;        // 파일 등록 날짜
    private String fileOriginName;  // 원본 파일명
}
