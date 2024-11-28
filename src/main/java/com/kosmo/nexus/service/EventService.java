package com.kosmo.nexus.service;


import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.EventDTO;
import com.kosmo.nexus.dto.SeasonDTO;

import java.util.List;

public interface EventService {
    // 이벤트 등록 메서드
    void registerEvent(EventDTO event);
    void registerSeason(SeasonDTO season);
    // 모든 이벤트 회차 조회
    List<SeasonDTO> getAllSeasons();

    int getSeasonCountByEventId(int eventId);

    List<EventDTO> getAllEvents();

    int getMaxEventId();

    int getBoardIdBySeasonId(int seasonId);

    void deleteBoardById(int boardId);

    void deleteSeason(int seasonId);       // c_season 삭제

    SeasonDTO getSeasonById(int seasonId);

    void updateSeason(SeasonDTO seasonDTO); // 시즌 수정

    void updateBoard(BoardDTO boardDTO);
    int getRoundNumberBySeasonId(int seasonId);


    List<SeasonDTO> searchSeasons(String findKeyword, String status);

    List<SeasonDTO> searchSeasonsByTitle(String findKeyword);
}

