package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.NoticeDTO;
import com.kosmo.nexus.dto.PagingDTO;
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

    // 공지사항 관련 메서드
    @Override
    public List<BoardDTO> selectNotificationList() {
        return boardMapper.selectNotificationList();
    }
    @Override
    public BoardDTO findNotificationById(int num) {
        return boardMapper.selectNotificationById(num);
    }
    @Override
    public void insertNotification(NoticeDTO noticeDTO) {
        log.info("공지사항 추가: {}", noticeDTO);
        boardMapper.insertNotification(noticeDTO);
    }
    @Override
    public void deleteNotification(int num) {
        log.info("공지사항 삭제 요청: {}", num);
        boardMapper.deleteNotification(num);
    }
    @Override
    public void updateNotification(NoticeDTO noticeDTO) {
        // 기본값 설정
        if (noticeDTO.getIsNotice() == null) {
            noticeDTO.setIsNotice(false); // 공지 여부 기본값 설정
        }

        // 공지사항 여부에 따라 카테고리 변경
        if (!noticeDTO.getIsNotice()) {
            noticeDTO.setBoardCategory("일반글"); // 공지사항이 아니면 일반 글로 변경
        }
        log.info("공지사항 수정: {}", noticeDTO);
        boardMapper.updateNotification(noticeDTO);
    }

    @Override
    public void increaseViewCount(int num) {
        log.info("조회수 증가 요청: {}", num);
        boardMapper.updateViewCount(num);
    }


    @Override
    public List<BoardDTO> selectNotificationList(PagingDTO pagingDTO) {
        return boardMapper.selectNotificationList(pagingDTO);
    }
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
        log.info("insertBoard 호출 전: {}", boardDTO);
        boardMapper.insertBoard(boardDTO);
        log.info("insertBoard 호출 후: boardId = {}", boardDTO.getBoardId());
    }



    @Override
    public List<BoardDTO> selectQnaList(PagingDTO pagingDTO) {
        return boardMapper.selectQnaList(pagingDTO);
    }

    @Override
    public int getTotalQnaCount(PagingDTO pagingDTO) {
        return boardMapper.getTotalQnaCount(pagingDTO);
    }

    @Override
    public BoardDTO findQnaById(int num) {
        return boardMapper.selectQnaById(num);
    }

    @Override
    public void insertQna(BoardDTO boardDTO) {
        log.info("QnA 추가: {}", boardDTO);
        boardMapper.insertQna(boardDTO);
    }

    @Override
    public void updateQna(BoardDTO boardDTO) {
        log.info("QnA 수정: {}", boardDTO);
        boardMapper.updateQna(boardDTO);
    }

    @Override
    public void deleteQna(int num) {
        log.info("QnA 삭제 요청: {}", num);
        boardMapper.deleteQna(num);
    }

    @Override
    public void increaseQnaViewCount(int num) {
        log.info("QnA 조회수 증가 요청: {}", num);
        boardMapper.updateQnaViewCount(num);
    }

    @Override
    public void updateAnswerStatus(int boardId, String answerStatus) {
        boardMapper.updateAnswerStatus(boardId, answerStatus);
    }

    @Override
    public void deleteBoardById(int boardId) {
        boardMapper.deleteBoardById(boardId);
    }
    @Override
    public void updateBoard(BoardDTO boardDTO) {
        boardMapper.updateBoard(boardDTO);
    }
}
