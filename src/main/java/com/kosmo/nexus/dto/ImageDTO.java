package com.kosmo.nexus.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class ImageDTO {
    private int imgId;             // Primary Key
    private String imgPath;        // 이미지 경로
    private String imgDate;        // 업로드 날짜
    private int imgSize;           // 파일 크기
    private String imgOriginName;  // 원본 파일 이름
    private int boardId;           // 연결된 게시물 ID
    private int contentOrder;      // 순서
    private String contentType;    // 'text' or 'image'
    private String contentData;    // 텍스트 내용 (contentType이 'text'인 경우 사용)
}
