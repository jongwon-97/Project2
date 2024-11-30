package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.EventDTO;
import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.dto.SeasonDTO;
import com.kosmo.nexus.mapper.BoardMapper;
import com.kosmo.nexus.mapper.EventMapper;
import com.kosmo.nexus.mapper.FileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class EventServiceImpl implements EventService{

    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private BoardMapper boardMapper;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private BoardService boardService;

    @Override
    public void registerEvent(EventDTO eventDTO) {
        // 이벤트 정보 삽입
        eventMapper.insertEvent(eventDTO);
        // 삽입된 이벤트 id 가져오기 (MyBatis의 selectKey 또는 useGeneratedKeys 사용)
    }
    @Override
    public void registerSeason(SeasonDTO seasonDTO) {
        log.info("registerSeason 호출: {}", seasonDTO);

        // c_board 데이터 삽입
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setBoardTitle(seasonDTO.getSeasonTitle());
        boardDTO.setBoardContent(seasonDTO.getSeasonInfo());
        boardDTO.setBoardCategory("Event");
        boardDTO.setMemberId("defaultUser");

        boardService.insertBoard(boardDTO); // c_board 삽입

        // board_id 설정
        int boardId = boardDTO.getBoardId();
        seasonDTO.setBoardId(boardId);

        // c_season 데이터 삽입
        eventMapper.insertSeason(seasonDTO);
        log.info("registerSeason 완료: {}", seasonDTO);
    }

    @Override
    public List<SeasonDTO> getAllSeasons() {
        List<SeasonDTO> seasonList = eventMapper.getAllSeasons();
        LocalDate today = LocalDate.now();

        for (SeasonDTO season : seasonList) {
            LocalDate endDate = LocalDate.parse(season.getSeasonEndDate());
            long dDay = ChronoUnit.DAYS.between(today, endDate);

            // 상태 업데이트
            if (dDay >= 0) {
                season.setSeasonState("모집중");
            } else {
                season.setSeasonState("마감");
            }

            // D-DAY 문자열 설정
            season.setDDay(dDay >= 0 ? "D-" + dDay : "D-0");
        }

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


    @Override
    public int getBoardIdBySeasonId(int seasonId) {
        return eventMapper.getBoardIdBySeasonId(seasonId);
    }

    @Override
    public void deleteSeason(int seasonId) {
        int boardId = getBoardIdBySeasonId(seasonId);

        // 1. 파일 삭제
        fileMapper.deleteFilesByBoardId(boardId);
        // 2. 게시판 삭제
        boardMapper.deleteBoardById(boardId);
        // 3. 시즌 삭제
        eventMapper.deleteSeason(seasonId);
    }
    @Override
    public void deleteBoardById(int boardId) {
        // 게시판 삭제 로직
        boardMapper.deleteBoardById(boardId);
    }

    @Override
    public SeasonDTO getSeasonById(int seasonId) {
        return eventMapper.getSeasonById(seasonId);
    }
    @Override
    public List<SeasonDTO> getSeasonsByBoardId(int boardId) {
        return eventMapper.findSeasonsByBoardId(boardId);
    }

    @Override
    public void updateSeason(SeasonDTO seasonDTO) {
        log.info("Season 업데이트 요청: {}", seasonDTO);
        eventMapper.updateSeason(seasonDTO);
    }


    @Override
    public void updateBoard(BoardDTO boardDTO) {
        boardMapper.updateBoard(boardDTO);
        log.info("BoardMapper를 통해 업데이트 수행: BoardId = {}", boardDTO.getBoardId());
    }

    @Override
    public int getRoundNumberBySeasonId(int seasonId) {
        Integer roundNumber = eventMapper.getRoundNumberBySeasonId(seasonId);
        return roundNumber != null ? roundNumber : 0; // null일 경우 기본값 0 반환
    }

    @Override
    public List<SeasonDTO> searchSeasons(String findKeyword, String status) {
        return eventMapper.searchSeasons(findKeyword, status);
    }

    @Override
    public List<SeasonDTO> searchSeasonsByTitle(String findKeyword) {
        return eventMapper.searchSeasonsByTitle(findKeyword); // 제목 검색 전용 매퍼 호출
    }

    @Override
    public void increaseSeasonViews(int seasonId) {
        eventMapper.updateSeasonViews(seasonId);
    }

    @Override
    public int applyEventByAdmin(List<String> memberIds, int seasonId, Long companyId) {
        return eventMapper.applyEventByAdmin(memberIds, seasonId, companyId);
    }

    @Override
    public List<MemberDTO> findAttentionMemberList(int seasonId, Long companyId) {
        return eventMapper.findAttentionMemberList(seasonId, companyId);
    }

    @Override
    public List<MemberDTO> findAbsenceMemberList(int seasonId, Long companyId) {
        return eventMapper.findAbsenceMemberList(seasonId, companyId);
    }

    @Override
    public List<MemberDTO> searchAbsenceMemberList(String search, String option, int seasonId, Long companyId) {
        return eventMapper.searchAbsenceMemberList(search, option, seasonId, companyId);
    }

    @Override
    public List<MemberDTO> searchAbsenceMemberListByDate(Map<String, Object> params) {
        return eventMapper.searchAbsenceMemberListByDate(params);
    }

    @Override
    public int deleteCancelMember(String memberId, int seasonId, Long companyId) {
        return eventMapper.deleteCancelMember(memberId, seasonId, companyId);
    }

    @Override
    public int findLimitCount(int seasonId) {
        return eventMapper.findLimitCount(seasonId);
    }

    @Override
    public int findAvailableCount(int seasonId) {
        return eventMapper.findAvailableCount(seasonId);
    }




}
