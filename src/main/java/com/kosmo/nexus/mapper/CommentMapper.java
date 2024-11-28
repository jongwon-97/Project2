package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.CommentDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<CommentDTO> getAllComments();

    void insertComment(CommentDTO comment);

    // 특정 게시판의 최상위 댓글 조회
    List<CommentDTO> findCommentsByBoardId(int boardId);
    // 특정 댓글의 대댓글 조회
    List<CommentDTO> findRepliesByParentId(Long parentId);


    String findCommentOwnerById(Long commentId); //댓글 삭제, 수정를 위한 댓글 작성자 확인
    void deleteReplies(Long commentId); //대댓글 삭제
    void deleteComment(Long commentId); //댓글 삭제

    void updateComment(CommentDTO comment); // 댓글 수정

    void deleteCommentsByBoardId(int boardId);

}
