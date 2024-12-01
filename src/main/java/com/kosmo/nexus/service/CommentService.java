package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.CommentDTO;

import java.util.List;

public interface CommentService {

    List<CommentDTO> getAllComments();


    void saveComment(CommentDTO comment);  // 댓글을 DB에 저장하는 메서드

    // 특정 게시판의 댓글 및 대댓글 조회
    List<CommentDTO> getCommentsByBoardId(int boardId);

    void deleteCommentAndReplies(Long commentId, String memberId) throws IllegalAccessException;

    //    // 댓글 수정
    void updateComment(CommentDTO comment, String memberId) throws IllegalAccessException;


    //공지 삭제 시 댓글 모두 삭제 처리
    void deleteCommentsByBoardId(int boardId);

    // seasonId로 댓글 및 대댓글 가져오기
    List<CommentDTO> getCommentsBySeasonId(int seasonId);

    void devDeleteCommentAndReplies(Long commentId);

    void devUpdateComment(CommentDTO comment);
}
