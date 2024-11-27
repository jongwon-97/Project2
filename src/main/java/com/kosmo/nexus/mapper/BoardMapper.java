package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.NoticeDTO;
import com.kosmo.nexus.dto.PagingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    // 공지사항 관련
    List<BoardDTO> selectNotificationList();
    BoardDTO selectNotificationById(@Param("num") int num); // 공지사항 상세보기
    void insertNotification(NoticeDTO noticeDTO); // 공지사항 작성
    void deleteNotification(@Param("num") int num);
    void updateNotification(NoticeDTO noticeDTO); // 업데이트 SQL 매핑
    void updateViewCount(@Param("num") int num); // 조회수 증가
    List<BoardDTO> selectNotificationList(PagingDTO pagingDTO);
    int getTotalNotificationCount(PagingDTO pagingDTO);
    int getTotalGeneralCount(PagingDTO pagingDTO);
    void insertBoard(BoardDTO boardDTO);

    List<BoardDTO> selectQnaList(PagingDTO pagingDTO); // QnA 목록 조회
    int getTotalQnaCount(PagingDTO pagingDTO); // QnA 총 게시글 수 조회
    BoardDTO selectQnaById(int num); // QnA 상세보기
    void insertQna(BoardDTO boardDTO); // QnA 작성
    void updateQna(BoardDTO boardDTO); // QnA 수정
    void deleteQna(int num); // QnA 삭제
    void updateQnaViewCount(int num); // QnA 조회수 증가

    void updateAnswerStatus(@Param("boardId") int boardId, @Param("answerStatus") String answerStatus);
}