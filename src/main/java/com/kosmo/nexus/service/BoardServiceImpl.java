package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.NoticeDTO;
import com.kosmo.nexus.dto.PagingDTO;
import com.kosmo.nexus.mapper.BoardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
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

    // 페이징된 공지사항 목록 조회
    @Override
    public List<BoardDTO> selectNotificationList(PagingDTO pagingDTO) {
        return boardMapper.selectNotificationList(pagingDTO); // 모든 게시물 반환
    }

    // 총 게시물 수 조회
    @Override
    public int getTotalNotificationCount(PagingDTO pagingDTO) {
        return boardMapper.getTotalNotificationCount(pagingDTO);
    }

    // 일반 글의 총 개수를 가져오는 메서드 구현
    @Override
    public int getTotalGeneralCount(PagingDTO pagingDTO) {
        return boardMapper.getTotalGeneralCount(pagingDTO);
    }

    @Override
    public void insertBoard(BoardDTO boardDTO) {
        boardMapper.insertBoard(boardDTO); // MyBatis Mapper 호출
    }


}
