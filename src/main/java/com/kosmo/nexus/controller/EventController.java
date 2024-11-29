package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.*;
import com.kosmo.nexus.service.AdminService;
import com.kosmo.nexus.service.BoardService;
import com.kosmo.nexus.service.EventService;
import com.kosmo.nexus.service.FileService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Autowired
    private AdminService adminService;

    // 이벤트 작성 폼
    @GetMapping("/board/event")
    public String showEventForm() {
        return "event/eventRegister";
    }

    // 이벤트 등록 처리
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
            @RequestParam(value = "images", required = false) MultipartFile[] images, // 다중 이미지 업로드
            @RequestParam(value = "texts", required = false) List<String> texts, // 텍스트 리스트
            @RequestParam(value = "file", required = false) MultipartFile file) {

        log.info("Request Params: eventId={}, title={}, images={}", eventId, title, images != null ? "있음" : "null");


        int nextRoundNumber;

        if (eventId == -1) { // 새로운 이벤트 등록
            EventDTO newEvent = new EventDTO();
            newEvent.setEventTitle(title);
            newEvent.setMemberId("defaultUser");

            eventService.registerEvent(newEvent);
            eventId = newEvent.getEventId();
            if (eventId == 0) {
                throw new RuntimeException("이벤트 ID 생성 실패");
            }
            nextRoundNumber = 1;
        } else { // 기존 이벤트의 ID로 처리
            nextRoundNumber = eventService.getSeasonCountByEventId(eventId) + 1;
        }

        // 시즌 상태 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDateParsed = LocalDate.parse(endDate, formatter);
        LocalDate today = LocalDate.now();
        String seasonState = endDateParsed.isBefore(today) || endDateParsed.isEqual(today) ? "마감" : "모집중";

        // 썸네일 업로드 처리
        String thumbnailPath = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailPath = saveFile(thumbnail, "thumbnails");
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
        seasonDTO.setSeasonState(seasonState);
        seasonDTO.setRoundNumber(nextRoundNumber);
        seasonDTO.setSeasonThumbnail(thumbnailPath);

        eventService.registerSeason(seasonDTO); // 여기에만 `boardService.insertBoard` 포함됨

        // Content Order 초기화
        int contentOrder = 1;

        // 텍스트 저장
        if (texts != null && !texts.isEmpty()) {
            for (String text : texts) {
                if (text != null && !text.isBlank()) {
                    ImageDTO content = new ImageDTO();
                    content.setBoardId(seasonDTO.getBoardId());
                    content.setContentType("text");
                    content.setContentOrder(contentOrder++);
                    content.setContentData(text.trim()); // 트림 처리
                    fileService.saveContent(content); // 텍스트 저장 서비스 호출
                }
            }
        }
        // 디버깅: images 배열이 null인지 확인
        log.info("images 배열 상태: {}", images != null ? images.length : "null");

        // 이미지 저장
        if (images != null) {
            String basePath = "src/main/resources/static/images"; // 기본 경로 설정
            Path folderPath = Paths.get(basePath);

            // 폴더 생성 확인 및 생성
            try {
                if (!Files.exists(folderPath)) {
                    Files.createDirectories(folderPath);
                    log.info("이미지 저장 폴더 생성 완료: {}", folderPath.toAbsolutePath());
                }
            } catch (IOException e) {
                log.error("이미지 저장 폴더 생성 중 예외 발생: {}", e.getMessage());
                throw new RuntimeException("이미지 저장 폴더 생성 실패", e);
            }

            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    // 파일명 생성
                    String originalFilename = image.getOriginalFilename();
                    String fileName = UUID.randomUUID() + "_" + originalFilename;

                    try {
                        // 저장 경로 설정
                        Path filePath = folderPath.resolve(fileName);

                        // 파일 저장
                        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        log.info("이미지 저장 성공: {}", filePath.toAbsolutePath());

                        // DB에 저장할 경로 생성
                        String imagePath = "/images/" + fileName;

                        // DB 저장용 DTO 생성
                        ImageDTO content = new ImageDTO();
                        content.setBoardId(seasonDTO.getBoardId());
                        content.setContentType("image");
                        content.setContentOrder(contentOrder++);
                        content.setImgPath(imagePath);
                        content.setImgOriginName(originalFilename);
                        content.setImgSize((int) image.getSize());

                        log.info("DB 저장 전 DTO: {}", content);

                        // DB에 저장
                        try {
                            fileService.saveContent(content);
                            log.info("DB 저장 성공: {}", content);
                        } catch (Exception e) {
                            log.error("DB 저장 중 예외 발생: {}", e.getMessage());
                            throw new RuntimeException("DB 저장 실패", e);
                        }

                    } catch (IOException e) {
                        log.error("이미지 저장 중 예외 발생: {}", e.getMessage());
                        throw new RuntimeException("이미지 저장 실패", e);
                    }
                } else {
                    log.error("이미지 파일이 비어 있거나 null입니다.");
                }
            }
        } else {
            log.error("images 배열이 null입니다.");
        }


        // 일반 파일 업로드 처리
        if (file != null && !file.isEmpty()) {
            String filePath = saveFile(file, "files");

            FileDTO fileDTO = new FileDTO();
            fileDTO.setFilePath(filePath);
            fileDTO.setBoardId(seasonDTO.getBoardId());
            fileDTO.setFileOriginName(file.getOriginalFilename());
            fileDTO.setFileSize(file.getSize());
            fileDTO.setFileDate(LocalDate.now().toString());

            fileService.saveFile(fileDTO);
        }
        return "redirect:/board/eventList";
    }//--------------------------------

    // 이벤트 목록
    @GetMapping("/board/eventList")
    public String getEventList(@RequestParam(required = false) String findKeyword,
                               @RequestParam(required = false) String status,
                               HttpServletRequest request,
                               Model model) {
        if (status == null || status.isEmpty()) {
            status = "모집중";
        }
        List<SeasonDTO> seasonList = eventService.searchSeasons(findKeyword, status);
        model.addAttribute("seasonList", seasonList);
        model.addAttribute("status", status);
        model.addAttribute("findKeyword", findKeyword);
        model.addAttribute("requestURI", request.getRequestURI()); // 현재 요청 URI 전달
        return "event/eventList";
    }//---------------------------------------
    // 이벤트 목록

    // 파일 저장 메서드
    private String saveFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null; // 파일이 없는 경우 null 반환
        }

        String basePath = "src/main/resources/static/" + folder;
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalFilename;

        try {
            Path folderPath = Paths.get(basePath);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            Path filePath = folderPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);


            return "/" + folder + "/" + fileName; // 저장된 파일 경로 반환
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다."); // 예외 발생
        }
    }


    // 이벤트 목록 JSON 반환
    @GetMapping("/api/events")
    @ResponseBody
    public List<EventDTO> getEventListJson() {
        return eventService.getAllEvents();
    }

    // 네비게이션 경로
    @GetMapping("/board/eventNavi")
    public String eventNavigation() {
        return "event/eventNavigation";
    }

    @GetMapping("/dev/board/eventList")
    public String devEventList(@RequestParam(required = false) String findKeyword, Model model) {
        // 제목 검색 전용 메서드 호출
        List<SeasonDTO> seasonList = eventService.searchSeasonsByTitle(findKeyword);

        model.addAttribute("seasonList", seasonList);   // 조회된 리스트
        model.addAttribute("findKeyword", findKeyword); // 검색 키워드

        return "event/devEventList"; // 템플릿 반환
    }

    @PostMapping("/dev/board/deleteEvent/{seasonId}")
    public String deleteEvent(@PathVariable("seasonId") int seasonId, Model model) {
        try {
            // 1. c_season과 연결된 board_id 조회
            int boardId = eventService.getBoardIdBySeasonId(seasonId);

            // 2. c_season과 연결된 파일 데이터 조회
            List<FileDTO> files = fileService.getFilesBySeasonId(seasonId);

            // 3. 실제 파일 삭제
            for (FileDTO file : files) {
                String filePath = "src/main/resources/static" + file.getFilePath();
                File targetFile = new File(filePath);

                if (targetFile.exists()) {
                    if (targetFile.delete()) {
//                        log.info("파일 삭제 성공: {}", filePath);
                    } else {
//                        log.warn("파일 삭제 실패: {}", filePath);
                    }
                } else {
//                    log.warn("파일을 찾을 수 없습니다: {}", filePath);
                }
            }

            // 4. c_file 데이터 삭제
            fileService.deleteFilesBySeasonId(seasonId);

            // 5. c_board 데이터 삭제
            boardService.deleteBoardById(boardId);

            // 6. c_season 데이터 삭제
            eventService.deleteSeason(seasonId);

//            log.info("행사 및 관련 데이터 삭제 성공: 시즌 ID = {}", seasonId);
            return "redirect:/dev/board/eventList";
        } catch (Exception e) {
//            log.error("행사 삭제 실패: 시즌 ID = {}, 에러: {}", seasonId, e.getMessage());
            model.addAttribute("errorMessage", "행사를 삭제하는 중 문제가 발생했습니다.");
            return "event/devEventList";
        }
    }
    @GetMapping("/board/event/detail/{seasonId}")
    public String getEventDetail(@PathVariable("seasonId") int seasonId, Model model) {
        // 시즌 데이터 가져오기
        SeasonDTO season = eventService.getSeasonById(seasonId);
        if (season == null) {
            throw new RuntimeException("Season 데이터를 찾을 수 없습니다: ID = " + seasonId);
        }

        // 조회수 증가
        eventService.increaseSeasonViews(seasonId);
        season.setSeasonViews(season.getSeasonViews() + 1); // 모델에 최신 조회수 반영

        // 이미지 및 텍스트 데이터 가져오기
        List<ImageDTO> contentList = fileService.getContentByBoardId(season.getBoardId());

        // 모델에 시즌 데이터 추가
        model.addAttribute("season", season);
        model.addAttribute("contentList", contentList); // 이미지 및 텍스트 정보

        return "event/eventDetail"; // eventDetail.html로 이동
    }

    @GetMapping("/board/endevent/detail/{seasonId}")
    public String getEndEventDetail(@PathVariable("seasonId") int seasonId, Model model) {
        // 시즌 데이터 가져오기
        SeasonDTO season = eventService.getSeasonById(seasonId);
        if (season == null) {
            throw new RuntimeException("Season 데이터를 찾을 수 없습니다: ID = " + seasonId);
        }

        // 시즌 상태 확인
        if (!"마감".equals(season.getSeasonState())) {
            throw new RuntimeException("해당 시즌은 마감되지 않았습니다: ID = " + seasonId);
        }

        // 조회수 증가
        eventService.increaseSeasonViews(seasonId);
        season.setSeasonViews(season.getSeasonViews() + 1); // 모델에 최신 조회수 반영

        // 모델에 시즌 데이터 추가
        model.addAttribute("season", season);

        return "event/eventEndDetail"; // eventEndDetail.html로 이동
    }


    @GetMapping("/dev/board/editEvent/{seasonId}")
    public String editEventForm(@PathVariable("seasonId") int seasonId, Model model) {
        // 시즌 데이터 가져오기
        SeasonDTO season = eventService.getSeasonById(seasonId);
        if (season == null) {
            throw new RuntimeException("Season 데이터를 찾을 수 없습니다: ID = " + seasonId);
        }
        model.addAttribute("season", season);

        // 이벤트 리스트 가져오기
        List<EventDTO> eventList = eventService.getAllEvents();
        if (eventList == null || eventList.isEmpty()) {
            eventList = new ArrayList<>(); // 비어 있는 리스트를 생성
        }
        model.addAttribute("eventList", eventList);

        return "event/editEventForm";
    }

    @PostMapping("/dev/board/updateEvent")
    public String updateEventPost(
            @ModelAttribute SeasonDTO seasonDTO,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Model model) {
        try {
            log.info("SeasonDTO 초기 상태: {}", seasonDTO);

            // 데이터베이스에서 boardId 가져오기
            if (seasonDTO.getBoardId() == 0) {
                Integer boardId = eventService.getBoardIdBySeasonId(seasonDTO.getSeasonId());
                if (boardId != null) {
                    seasonDTO.setBoardId(boardId);
                } else {
                    log.warn("SeasonDTO의 boardId가 데이터베이스에서 조회되지 않았습니다.");
                }
            }

            // 데이터베이스에서 roundNumber 가져오기
            if (seasonDTO.getRoundNumber() == 0) { // 기본값 0일 때만 가져옴
                int roundNumber = eventService.getRoundNumberBySeasonId(seasonDTO.getSeasonId());
                seasonDTO.setRoundNumber(roundNumber); // roundNumber 설정
            }

            // 썸네일 업로드 처리
            if (thumbnail != null && !thumbnail.isEmpty()) {
                deleteExistingFile(seasonDTO.getSeasonThumbnail());
                String thumbnailPath = saveFile(thumbnail, "thumbnails");
                seasonDTO.setSeasonThumbnail(thumbnailPath);
            }

            // 시즌 정보 업데이트
            eventService.updateSeason(seasonDTO);

            // 상태 업데이트: 현재 날짜와 종료 날짜 비교
            LocalDate endDate = LocalDate.parse(seasonDTO.getSeasonEndDate());
            LocalDate today = LocalDate.now();
            if (!today.isBefore(endDate)) {
                seasonDTO.setSeasonState("마감");
            } else {
                seasonDTO.setSeasonState("모집중");
            }

            // c_board 테이블 업데이트
            if (seasonDTO.getBoardId() > 0) {
                BoardDTO boardDTO = new BoardDTO();
                boardDTO.setBoardId(seasonDTO.getBoardId());
                boardDTO.setBoardTitle(seasonDTO.getSeasonTitle());
                boardDTO.setBoardContent(seasonDTO.getSeasonInfo());
                boardDTO.setBoardCategory("Event");
                boardDTO.setBoardCreateDate(LocalDate.now().toString());
                boardDTO.setDisclosureStatus("공개");

                boardService.updateBoard(boardDTO);
                log.info("Board 업데이트 완료: BoardId = {}", boardDTO.getBoardId());
            } else {
                log.warn("유효한 boardId가 없어 c_board 업데이트를 생략합니다.");
            }

            // 수정된 데이터를 다시 로드
            List<SeasonDTO> seasonList = eventService.getAllSeasons();
            model.addAttribute("seasonList", seasonList);

            return "redirect:/dev/board/eventList";
        } catch (Exception e) {
            log.error("Season 업데이트 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "수정 중 문제가 발생했습니다.");
            return "event/editEventForm";
        }
    }

    @GetMapping("/admin/event/apply/{seasonId}")
    private String getApplyForm(@PathVariable("seasonId") int seasonId, HttpSession ses, Model model){
        Long sesCompanyId = getLoginUserCompanyId(ses, model);
        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();
        List<String> departmentsInCompany = adminService.findDepartmentByCompanyId(sesCompanyId);
        List<String> ranksInCompany = adminService.findRankByCompanyId(sesCompanyId);
        List<MemberDTO> listMember= adminService.findMemberList(sesCompanyId);
        List<String> departments = listMember.stream()
                .map(MemberDTO::getMemberDepartment) // 단일 값을 추출
                .distinct() // 중복 제거
                .sorted() // 오름차순 정렬
                .collect(Collectors.toList()); // List<String>으로 수집
        List<String> ranks = listMember.stream()
                .map(MemberDTO::getMemberRank) // 단일 값을 추출
                .distinct() // 중복 제거
                .sorted() // 오름차순 정렬
                .collect(Collectors.toList()); // List<String>으로 수집

        model.addAttribute("listMember", listMember);
        model.addAttribute("departments", departments);
        model.addAttribute("ranks", ranks);
        model.addAttribute("departmentsInCompany", departmentsInCompany);
        model.addAttribute("ranksInCompany", ranksInCompany);
        model.addAttribute("sesLoginId", sesLoginId);
        model.addAttribute("seasonId",seasonId);

        return "event/adminApplyForm";
    }

    @PostMapping("/admin/event/apply/{seasonId}")
    public String getApplyMember(@RequestParam List<String> memberIds,
                                 @PathVariable("seasonId") int seasonId,
                                 HttpSession ses, Model model){
        log.info("memberIds=={}",memberIds);
        return "redirect:/admin/event/apply/"+seasonId;
    }

    private void deleteExistingFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            String fullPath = "src/main/resources/static" + filePath; // 파일 경로 완성
            File existingFile = new File(fullPath);
            if (existingFile.exists() && existingFile.isFile()) {
                if (existingFile.delete()) {
                    log.info("기존 파일 삭제 성공: {}", fullPath);
                } else {
                    log.warn("기존 파일 삭제 실패: {}", fullPath);
                }
            } else {
                log.warn("삭제하려는 파일이 존재하지 않음: {}", fullPath);
            }
        }
    }

    public Long getLoginUserCompanyId(HttpSession ses, Model model){
        // 세션에서 loginUser 객체 가져오기
        LoginDTO loginUser = (LoginDTO) ses.getAttribute("loginUser");
        if (loginUser == null) {
            message(model, "정상적인 로그인 정보가 아닙니다.", "/logout");
            return null;
        }
        Long companyId = loginUser.getCompanyId();
        if (companyId == null) {     // memberId가 없는 경우
            message(model, "정상적인 로그인 정보가 아닙니다.", "/logout");
            return null;
        }
        return companyId;
    }


    public String message(Model model, String msg, String loc){
        model.addAttribute("msg", msg);
        model.addAttribute("loc", loc);
        return "message";
    }
}
