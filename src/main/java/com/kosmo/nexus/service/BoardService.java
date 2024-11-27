package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.NoticeDTO;
import com.kosmo.nexus.dto.PagingDTO;

import java.util.List;

public interface BoardService {
    List<BoardDTO> selectNotificationList();
    BoardDTO findNotificationById(int num);
    void insertNotification(NoticeDTO noticeDTO); // 공지사항 작성
    void deleteNotification(int num);
    void updateNotification(NoticeDTO noticeDTO);
    void increaseViewCount(int num);

    // 페이징된 공지사항 목록 조회
    List<BoardDTO> selectNotificationList(PagingDTO pagingDTO);
    // 총 게시물 수 조회
    int getTotalNotificationCount(PagingDTO pagingDTO);

    // 일반 글의 총 개수를 가져오는 메서드
    int getTotalGeneralCount(PagingDTO paging);

    void insertBoard(BoardDTO boardDTO);

}
