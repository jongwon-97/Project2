package com.kosmo.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentDTO {

    private Long commentId;           // 댓글 ID
    private Long boardId;             // 게시판 ID
    private String memberId;          // 작성자 ID
    private String commentContent;    // 댓글 내용
    private LocalDateTime commentTime; // 작성 시간
    private Long parentId;            // 부모 댓글 ID (null이면 최상위 댓글) (대댓글의 경우 부모 댓글의 commentId가 들어감)
    private List<CommentDTO> replies; // 대댓글 리스트
}
