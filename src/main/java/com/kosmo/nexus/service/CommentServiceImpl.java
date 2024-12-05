package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.CommentDTO;
import com.kosmo.nexus.mapper.BoardMapper; // BoardMapper 추가
import com.kosmo.nexus.mapper.CommentMapper;
import com.kosmo.nexus.mapper.EventMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final BoardMapper boardMapper; // BoardMapper 필드 추가
    private final EventMapper eventMapper;

    // 생성자를 통해 의존성 주입
    public CommentServiceImpl(CommentMapper commentMapper, BoardMapper boardMapper, EventMapper eventMapper) {
        this.commentMapper = commentMapper;
        this.boardMapper = boardMapper;
        this.eventMapper = eventMapper;
    }

    @Override
    public List<CommentDTO> getAllComments() {
        return commentMapper.getAllComments();
    }

    @Override
    public void saveComment(CommentDTO comment) { // 댓글과 대댓글을 한 번에 처리
        if (comment.getParentId() == null) {
            // 부모 댓글일 경우(null)
            commentMapper.insertComment(comment);
        } else {
            // 대댓글일 경우
            commentMapper.insertComment(comment);
        }

        // 댓글 추가 시 QnA의 답변 여부를 "Y"로 업데이트
        boardMapper.updateAnswerStatus(comment.getBoardId(), "Y");
    }

    @Override
    public List<CommentDTO> getCommentsByBoardId(int boardId) {
        List<CommentDTO> comments = commentMapper.findCommentsByBoardId(boardId);
        // 대댓글을 각 댓글에 연관시킴
        for (CommentDTO comment : comments) {
            if (comment.getParentId() == null) {
                List<CommentDTO> replies = commentMapper.findRepliesByParentId(comment.getCommentId());
                comment.setReplies(replies);  // 대댓글 리스트를 댓글에 설정
            }
        }
        return comments;
    }

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
    }

    @Override
    public void updateComment(CommentDTO comment, String memberId) throws IllegalAccessException {
        // 작성자 검증
        String ownerId = commentMapper.findCommentOwnerById(comment.getCommentId());
        if (ownerId == null || !ownerId.equals(memberId)) {
            throw new IllegalAccessException("수정 권한이 없습니다.");
        }
        // 댓글 수정
        commentMapper.updateComment(comment);
    }

    // 공지 삭제 시 댓글 모두 삭제 처리
    @Override
    public void deleteCommentsByBoardId(int boardId) {
        try {
            commentMapper.deleteCommentsByBoardId(boardId);
        } catch (Exception e) {
            throw new RuntimeException("댓글 삭제 중 오류 발생");
        }
    }
    // seasonId로 댓글 및 대댓글 가져오기
    @Override
    public List<CommentDTO> getCommentsBySeasonId(int seasonId) {
        // seasonId를 통해 boardId 조회
        Integer boardId = eventMapper.getBoardIdBySeasonId(seasonId);
        if (boardId == null) {
            throw new RuntimeException("해당 seasonId에 연결된 boardId를 찾을 수 없습니다: " + seasonId);
        }

        // boardId로 댓글 데이터 조회
        return commentMapper.selectCommentsByBoardId(boardId);
    }

    @Override
    public void devDeleteCommentAndReplies(Long commentId) {
        // 작성자 검증 로직 제거
        commentMapper.deleteReplies(commentId); // 대댓글 삭제
        commentMapper.deleteComment(commentId); // 댓글 삭제
    }

    @Override
    public void devUpdateComment(CommentDTO comment) {
        // 작성자 검증 로직 제거
        commentMapper.updateComment(comment);
    }


}
