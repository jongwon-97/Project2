package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.NoticeDTO;
import com.kosmo.nexus.dto.PagingDTO;

import java.util.List;

public interface BoardService {
    // 공지사항 관련
    List<BoardDTO> selectNotificationList();

    BoardDTO findNotificationById(int num);

    void insertNotification(NoticeDTO noticeDTO); // 공지사항 작성

    void deleteNotification(int num);

    void updateNotification(NoticeDTO noticeDTO);

    void increaseViewCount(int num);

    List<BoardDTO> selectNotificationList(PagingDTO pagingDTO);

    int getTotalNotificationCount(PagingDTO pagingDTO);
    // 일반 글의 총 개수를 가져오는 메서드
    int getTotalGeneralCount(PagingDTO paging);

    void insertBoard(BoardDTO boardDTO);

    List<BoardDTO> selectQnaList(PagingDTO pagingDTO); // QnA 목록 조회
    int getTotalQnaCount(PagingDTO pagingDTO); // QnA 총 게시글 수 조회
    BoardDTO findQnaById(int num); // QnA 상세보기
    void insertQna(BoardDTO boardDTO); // QnA 작성
    void updateQna(BoardDTO boardDTO); // QnA 수정
    void deleteQna(int num); // QnA 삭제
    void increaseQnaViewCount(int num); // QnA 조회수 증가

    void updateAnswerStatus(int boardId, String answerStatus);
}
