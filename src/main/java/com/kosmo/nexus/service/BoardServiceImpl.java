package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.NoticeDTO;
import com.kosmo.nexus.mapper.BoardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;

    public BoardServiceImpl(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    @Override
    public List<BoardDTO> selectNotificationList() {
        return boardMapper.selectNotificationList();
    }
    @Override
    public BoardDTO findNotificationById(int num) {
        return boardMapper.selectNotificationById(num);
    }


    // 공지사항 작성 메서드에 NoticeDTO 사용
    public void insertNotification(NoticeDTO noticeDTO) {
        log.info("공지사항 추가: {}", noticeDTO);
        boardMapper.insertNotification(noticeDTO); // NoticeDTO 전달
    }
    @Override
    public void deleteNotification(int num) {
        log.info("공지사항 삭제 요청: {}", num);
        boardMapper.deleteNotification(num);
    }
    @Override
    public void updateNotification(NoticeDTO noticeDTO) {
        log.info("공지사항 수정: {}", noticeDTO);
        boardMapper.updateNotification(noticeDTO); // 매퍼 호출
    }

    @Override
    public void increaseViewCount(int num) {
        log.info("조회수 증가 요청: {}", num);
        boardMapper.updateViewCount(num);
    }


}
