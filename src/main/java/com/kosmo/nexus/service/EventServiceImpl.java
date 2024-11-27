package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.EventDTO;
import com.kosmo.nexus.dto.SeasonDTO;
import com.kosmo.nexus.mapper.EventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements EventService{

    @Autowired
    private EventMapper eventMapper;

    @Override
    public void registerEvent(EventDTO eventDTO) {
        // 이벤트 정보 삽입
        eventMapper.insertEvent(eventDTO);
        // 삽입된 이벤트 id 가져오기 (MyBatis의 selectKey 또는 useGeneratedKeys 사용)
    }
    @Override
    public void registerSeason(SeasonDTO seasonDTO) {
        // 시즌 정보 삽입
        eventMapper.insertSeason(seasonDTO);
    }

    @Override
    public List<SeasonDTO> getAllSeasons() {
        List<SeasonDTO> seasonList = eventMapper.getAllSeasons();
        // 로그로 결과 확인
        System.out.println("Retrieved seasonList: " + seasonList);
        return seasonList;
    }


    @Override
    public int getSeasonCountByEventId(int eventId) {
        return eventMapper.getSeasonCountByEventId(eventId);
    }
    @Override
    public List<EventDTO> getAllEvents() {
        return eventMapper.selectAllEvents(); // 모든 이벤트를 조회하는 메서드 호출
    }
    @Override
    public int getMaxEventId() {
        return eventMapper.selectMaxEventId(); // 현재 가장 큰 event_id 조회
    }


}
