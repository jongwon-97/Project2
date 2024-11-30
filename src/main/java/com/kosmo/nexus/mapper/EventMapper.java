package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.EventDTO;
import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.dto.SeasonDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface EventMapper {
    //이벤트 정보 삽입
    void insertEvent(EventDTO eventDTO);
    //회차 정보 삽입
    void insertSeason(SeasonDTO seasonDTO);

    //모든 시즌 데이터를 가져옴
    List<SeasonDTO> getAllSeasons();

    int getSeasonCountByEventId(int eventId);

    List<EventDTO> selectAllEvents();

    int selectMaxEventId();

    // 특정 season_id에 해당하는 board_id 조회
    int getBoardIdBySeasonId(@Param("seasonId") int seasonId);

    // 특정 season_id 데이터 삭제
    void deleteSeason(@Param("seasonId") int seasonId);

    SeasonDTO getSeasonById(int seasonId);

    void updateSeason(SeasonDTO seasonDTO);

    Integer getRoundNumberBySeasonId(int seasonId);

    List<SeasonDTO> searchSeasons(String findKeyword, String status);

    List<SeasonDTO> searchSeasonsByTitle(String findKeyword);

    void updateSeasonViews(@Param("seasonId") int seasonId);

    int applyEventByAdmin(List<String> memberIds, int seasonId, Long companyId);
    List<MemberDTO> findAttentionMemberList(int seasonId, Long companyId);
    List<MemberDTO> findAbsenceMemberList(int seasonId, Long companyId);
    List<MemberDTO> searchAbsenceMemberList(String search, String option, int seasonId, Long companyId);
    List<MemberDTO> searchAbsenceMemberListByDate(Map<String, Object> params);
    int deleteCancelMember(String memberId, int seasonId, Long companyId);
    int findLimitCount(int seasonId);
    int findAvailableCount(int seasonId);
}
