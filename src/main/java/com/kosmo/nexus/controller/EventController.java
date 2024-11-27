package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.EventDTO;
import com.kosmo.nexus.dto.FileDTO;
import com.kosmo.nexus.dto.SeasonDTO;
import com.kosmo.nexus.service.BoardService;
import com.kosmo.nexus.service.EventService;
import com.kosmo.nexus.service.FileService;
import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class EventController {

    @Autowired
    private EventService eventService;
    @Autowired
    private BoardService boardService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ServletContext servletContext;


    @GetMapping("/board/event") //작성 폼 보여주기
    public String showEventForm() {
        return "event/eventRegister";
    }

    @PostMapping("/board/event")
    public String registerEventPost(
            @RequestParam("event_id") int eventId,
            @RequestParam("title") String title,
            @RequestParam("start_date") String startDate,
            @RequestParam("end_date") String endDate,
            @RequestParam("recruitment_count") int recruitmentCount,
            @RequestParam("fee") int fee,
            @RequestParam("season_info") String seasonInfo,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        int nextRoundNumber;

        if (eventId == -1) { // 새로운 이벤트 등록은 html에서 -1로 설정했음
            // 새로운 이벤트 등록 로직
            EventDTO newEvent = new EventDTO();
            newEvent.setEventTitle(title);
            newEvent.setMemberId("defaultUser");  // 기본 사용자 설정

            // 새로운 이벤트 등록
            eventService.registerEvent(newEvent);
            eventId = newEvent.getEventId();  // 등록된 새로운 이벤트 ID 가져오기
            if (eventId == 0) {
                throw new RuntimeException("이벤트 ID를 가져오지 못했습니다. 데이터베이스 등록에 문제가 발생했습니다.");
            }
            nextRoundNumber = 1; // 새로운 이벤트는 항상 1회차부터 시작
        } else {
            // 기존 이벤트의 ID로 처리
            nextRoundNumber = eventService.getSeasonCountByEventId(eventId) + 1;
        }

        // 종료 날짜 계산
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDateParsed = LocalDate.parse(endDate, formatter);
        LocalDate today = LocalDate.now();

        // 상태 설정
        String seasonState = endDateParsed.isBefore(today) || endDateParsed.isEqual(today) ? "마감" : "모집중";

        // 썸네일 업로드 처리
        String thumbnailPath = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String folderPath = "/static/thumbnails";
            Path path = Paths.get(new File("src/main/resources" + folderPath).getAbsolutePath());


            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path); // 경로가 없으면 디렉토리 생성
                } catch (IOException e) {
                    log.error("썸네일 디렉토리 생성 실패: {}", e.getMessage());
                    throw new RuntimeException("썸네일 디렉토리 생성에 실패했습니다.");
                }
            }

            // 파일 이름 생성 (UUID + 원본 파일명)
            String originalFilename = thumbnail.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

            // 파일 저장
            Path destination = path.resolve(fileName);
            try {
                Files.copy(thumbnail.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                thumbnailPath = "/thumbnails/" + fileName; // 저장된 파일 경로 설정
                log.info("Thumbnail successfully saved. Path: {}", thumbnailPath);
            } catch (IOException e) {
                log.error("썸네일 저장 실패: {}", e.getMessage());
                throw new RuntimeException("썸네일 저장에 실패했습니다.");
            }
        }

        // 시즌 정보 등록
        SeasonDTO seasonDTO = new SeasonDTO();
        seasonDTO.setEventId(eventId);
        seasonDTO.setSeasonTitle(title);
        seasonDTO.setSeasonStartDate(startDate);
        seasonDTO.setSeasonEndDate(endDate);
        seasonDTO.setSeasonLimit(recruitmentCount);
        seasonDTO.setSeasonFee(fee);
        seasonDTO.setSeasonInfo(seasonInfo);
        seasonDTO.setSeasonState(seasonState);  // 상태 설정
        seasonDTO.setRoundNumber(nextRoundNumber);
        seasonDTO.setSeasonThumbnail(thumbnailPath); // 썸네일 경로 설정

        eventService.registerSeason(seasonDTO);
        log.info("Thumbnail path: {}", thumbnailPath);


        // c_board 테이블에 게시글 저장
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setBoardTitle(title); // 제목
        boardDTO.setBoardContent(seasonInfo); // 게시글 내용에 시즌 설명 저장
        boardDTO.setBoardCategory("Event"); // 카테고리 설정
        boardDTO.setMemberId("defaultUser"); // 기본 사용자 설정

        boardService.insertBoard(boardDTO); // 게시글 저장
        int boardId = boardDTO.getBoardId();

        // 파일 업로드 처리 (선택 사항)
        if (image != null && !image.isEmpty()) {
            // 이미지 저장 로직 추가
        }
        if (file != null && !file.isEmpty()) {
            String uploadPath = new File("src/main/resources/static/files").getAbsolutePath();
            String fileName = file.getOriginalFilename();

            try {
                // 파일 저장
                File saveFile = new File(uploadPath, fileName);
                if (!saveFile.getParentFile().exists()) {
                    saveFile.getParentFile().mkdirs();
                }
                file.transferTo(saveFile);

                // 파일 정보 저장
                FileDTO fileDTO = new FileDTO();
                fileDTO.setFilePath("/upload/" + fileName); // 정적 리소스 경로
                fileDTO.setBoardId(boardId); // 게시글 ID 연결
                fileDTO.setFileOriginName(fileName);
                fileDTO.setFileSize(file.getSize());
                fileDTO.setFileDate(LocalDate.now().toString()); // 현재 날짜 저장

                fileService.saveFile(fileDTO); // 파일 정보 저장

            } catch (IOException e) {
                log.error("파일 저장 실패: {}", e.getMessage());
                throw new RuntimeException("파일 저장에 실패했습니다.");
            }
        }
        // 이벤트 목록 페이지로 리다이렉트
        return "redirect:/board/eventList";
    }//--------------------------

    @GetMapping("/board/eventList")
    public String getEventList(Model model) {
        // 이벤트 목록을 가져옴 (회차 정보 포함)
        List<SeasonDTO> seasonList = eventService.getAllSeasons();
        log.info("seasonList=={}", seasonList);
        // 화면에 데이터를 전달하기 위해 모델에 추가
        model.addAttribute("seasonList", seasonList);

        return "event/eventList";  // 템플릿 파일로 이동
    }

    // 이벤트 목록을 JSON으로 반환하는 API 추가
    @GetMapping("/api/events")
    @ResponseBody
    public List<EventDTO> getEventListJson() {
        // 이벤트 목록을 JSON으로 반환
        return eventService.getAllEvents();
    }
    //네비게이션 경로
    @GetMapping("/board/eventNavi")
    public String eventNavigation(){
        return "event/eventNavigation";
    }

}