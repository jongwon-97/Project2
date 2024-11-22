package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.CommentDTO;
import com.kosmo.nexus.mapper.CommentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService{

    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }
    @Override
    public List<CommentDTO> getAllComments() {
        return commentMapper.getAllComments();
    }//-------------------------------

    @Override
    public void saveComment(CommentDTO comment) { //댓글과 대댓글을 한 번에 처리
        if (comment.getParentId() == null) {
            // 부모 댓글일 경우(null)
            commentMapper.insertComment(comment);
        } else {
            // 대댓글일 경우
            commentMapper.insertComment(comment);
        }
    }//---------------------------

    @Override
    public List<CommentDTO> getCommentsByBoardId(Long boardId) {
        List<CommentDTO> comments = commentMapper.findCommentsByBoardId(boardId);
        // 대댓글을 각 댓글에 연관시킴
        for (CommentDTO comment : comments) {
            if (comment.getParentId() == null) {
                List<CommentDTO> replies = commentMapper.findRepliesByParentId(comment.getCommentId());
                comment.setReplies(replies);  // 대댓글 리스트를 댓글에 설정
            }
        }
        return comments;
    }//-------------------------------
    @Override
    public void deleteCommentAndReplies(Long commentId, String memberId) throws IllegalAccessException {
        // 댓글 작성자 확인
        String ownerId = commentMapper.findCommentOwnerById(commentId);
        if (ownerId == null) {
            throw new IllegalAccessException("댓글이 존재하지 않습니다.");
        }
        if (!memberId.equals(ownerId)) {
            throw new IllegalAccessException("댓글 삭제 권한이 없습니다.");
        }
        // 대댓글 및 댓글 삭제
        commentMapper.deleteReplies(commentId); // 대댓글 삭제
        commentMapper.deleteComment(commentId); // 댓글 삭제
    }//--------------------------------

    @Override
    public void updateComment(CommentDTO comment, String memberId) throws IllegalAccessException {
        // 작성자 검증
        String ownerId = commentMapper.findCommentOwnerById(comment.getCommentId());
        if (ownerId == null || !ownerId.equals(memberId)) {
            throw new IllegalAccessException("수정 권한이 없습니다.");
        }
        // 댓글 수정
        commentMapper.updateComment(comment);
    }//---------------------------------------




}
