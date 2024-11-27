package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.NoticeDTO;
import com.kosmo.nexus.dto.PagingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    List<BoardDTO> selectNotificationList();
    BoardDTO selectNotificationById(@Param("num") int num); // 공지사항 상세보기
    void insertNotification(NoticeDTO noticeDTO); // 공지사항 작성 시 NoticeDTO 사용
    void deleteNotification(@Param("num") int num);
    void updateNotification(NoticeDTO noticeDTO); // 업데이트 SQL 매핑
    void updateViewCount(@Param("num") int num); //조회수 증가

    // 페이징 처리된 공지사항 목록 조회
    List<BoardDTO> selectNotificationList(PagingDTO pagingDTO);
    // 총 게시물 수 조회
    int getTotalNotificationCount(PagingDTO pagingDTO);

    int getTotalGeneralCount(PagingDTO pagingDTO);

    void insertBoard(BoardDTO boardDTO);
}
