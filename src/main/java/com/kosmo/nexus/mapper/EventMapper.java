package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.EventDTO;
import com.kosmo.nexus.dto.SeasonDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
}
