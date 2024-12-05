package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.EventDTO;
import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.dto.SeasonDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    List<SeasonDTO> getSeasonsByBoardId(int boardId);

    void updateSeason(SeasonDTO seasonDTO); // 시즌 수정

    void updateBoard(BoardDTO boardDTO);
    int getRoundNumberBySeasonId(int seasonId);


    List<SeasonDTO> searchSeasons(String findKeyword, String status);

    List<SeasonDTO> searchSeasonsByTitle(String findKeyword);

    void increaseSeasonViews(int seasonId);

    int applyEventByAdmin(List<String> memberIds, int seasonId, Long companyId);
    int applyEventByUser(String memberId, int seasonId, Long companyId);
    List<MemberDTO> findAttentionMemberList(int seasonId, Long companyId);
    MemberDTO findAttentionMember(int seasonId, Long companyId, String memberId);

    List<MemberDTO> findAbsenceMemberList(int seasonId, Long companyId);
    List<MemberDTO> searchAbsenceMemberList(String search, String option, int seasonId, Long companyId);
    List<MemberDTO> searchAbsenceMemberListByDate(Map<String, Object> params);
    int deleteCancelMember(String memberId, int seasonId, Long companyId);
    int findLimitCount(int seasonId);
    int findAvailableCount(int seasonId);

    List<SeasonDTO> getSeasonsByEventId(int eventId);

    int getEventIdBySeasonId(int seasonId);

    List<MemberDTO> findAllAttentionMemberList(@Param("seasonId") int seasonId, @Param("companyId") Long companyId);

}

