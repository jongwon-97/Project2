package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.NoticeDTO;

import java.util.List;

public interface BoardService {
    List<BoardDTO> selectNotificationList();
    BoardDTO findNotificationById(int num);
    void insertNotification(NoticeDTO noticeDTO); // 공지사항 작성
    void deleteNotification(int num);
    void updateNotification(NoticeDTO noticeDTO);
    void increaseViewCount(int num);

}
