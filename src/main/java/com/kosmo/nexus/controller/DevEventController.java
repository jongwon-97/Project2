package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.*;
import com.kosmo.nexus.mapper.BoardMapper;
import com.kosmo.nexus.service.BoardService;
import com.kosmo.nexus.service.CommentService;
import com.kosmo.nexus.service.EventService;
import com.kosmo.nexus.service.FileService;
import jakarta.servlet.ServletContext;
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
import java.util.*;

@Controller
@Slf4j
@RequestMapping("/dev")
public class DevEventController {

    @Autowired
    private EventService eventService;
    @Autowired
    private BoardService boardService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private BoardMapper boardMapper;
    @Autowired
    private CommentService commentService;

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
            @RequestParam(value = "images[]", required = false) MultipartFile[] images,
            @RequestParam(value = "texts", required = false) List<String> texts,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        log.info("Request Params: eventId={}, title={}, images={}", eventId, title, images != null ? "있음" : "null");

        // 새로운 이벤트 등록 처리
        int nextRoundNumber;
        if (eventId == -1) {
            EventDTO newEvent = new EventDTO();
            newEvent.setEventTitle(title);
            newEvent.setMemberId("defaultUser");
            eventService.registerEvent(newEvent);

            eventId = newEvent.getEventId();
            if (eventId == 0) {
                throw new RuntimeException("이벤트 ID 생성 실패");
            }
            nextRoundNumber = 1;
        } else {
            nextRoundNumber = eventService.getSeasonCountByEventId(eventId) + 1;
        }

        // 시즌 상태 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDateParsed = LocalDate.parse(endDate, formatter);
        LocalDate today = LocalDate.now();
        String seasonState = endDateParsed.isBefore(today) || endDateParsed.isEqual(today) ? "마감" : "모집중";

        // 썸네일 저장
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
        eventService.registerSeason(seasonDTO);

        // 텍스트와 이미지를 순서대로 처리
        List<Object> mixedContents = new ArrayList<>();
        int maxIndex = Math.max(texts != null ? texts.size() : 0, images != null ? images.length : 0);

        for (int i = 0; i < maxIndex; i++) {
            if (texts != null && i < texts.size()) {
                mixedContents.add(texts.get(i));
            }
            if (images != null && i < images.length) {
                mixedContents.add(images[i]);
            }
        }

        int contentOrder = 1;
        for (Object content : mixedContents) {
            if (content instanceof String) { // 텍스트 처리
                String text = (String) content;
                if (text != null && !text.isBlank()) {
                    ImageDTO textContent = new ImageDTO();
                    textContent.setBoardId(seasonDTO.getBoardId());
                    textContent.setContentType("text");
                    textContent.setContentOrder(contentOrder++);
                    textContent.setContentData(text.trim());
                    fileService.saveContent(textContent);
                }
            } else if (content instanceof MultipartFile) { // 이미지 처리
                MultipartFile image = (MultipartFile) content;
                if (image != null && !image.isEmpty()) {
                    String originalFilename = image.getOriginalFilename();
                    String fileName = UUID.randomUUID() + "_" + originalFilename;
                    String basePath = "src/main/resources/static/images";

                    try {
                        Path folderPath = Paths.get(basePath);
                        if (!Files.exists(folderPath)) {
                            Files.createDirectories(folderPath);
                        }
                        Path filePath = folderPath.resolve(fileName);
                        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        ImageDTO imageContent = new ImageDTO();
                        imageContent.setBoardId(seasonDTO.getBoardId());
                        imageContent.setContentType("image");
                        imageContent.setContentOrder(contentOrder++);
                        imageContent.setImgPath("/images/" + fileName);
                        imageContent.setImgOriginName(originalFilename);
                        imageContent.setImgSize((int) image.getSize());
                        fileService.saveContent(imageContent);
                    } catch (IOException e) {
                        log.error("이미지 저장 실패: {}", e.getMessage());
                        throw new RuntimeException("이미지 저장 실패", e);
                    }
                }
            }
        }

        // 파일 저장
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

        return "redirect:/dev/board/eventList";
    }


    // 이벤트 목록
//    @GetMapping("/board/eventList")
//    public String getEventList(@RequestParam(required = false) String findKeyword,
//                               @RequestParam(required = false) String status,
//                               HttpServletRequest request,
//                               Model model) {
//        if (status == null || status.isEmpty()) {
//            status = "모집중";
//        }
//        List<SeasonDTO> seasonList = eventService.searchSeasons(findKeyword, status);
//        model.addAttribute("seasonList", seasonList);
//        model.addAttribute("status", status);
//        model.addAttribute("findKeyword", findKeyword);
//        model.addAttribute("requestURI", request.getRequestURI()); // 현재 요청 URI 전달
//        return "event/devEventList";
//    }//---------------------------------------
//    // 이벤트 목록

    // 파일 저장 메서드
    private String saveFile(MultipartFile file, String folder) {
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

            log.info("파일 저장 성공: {}", filePath);
            return "/" + folder + "/" + fileName;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new RuntimeException("파일 저장에 실패했습니다.");
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

    @GetMapping("/board/eventList")
    public String devEventList(@RequestParam(required = false) String findKeyword, Model model) {
        // 제목 검색 전용 메서드 호출
        List<SeasonDTO> seasonList = eventService.searchSeasonsByTitle(findKeyword);

        model.addAttribute("seasonList", seasonList);   // 조회된 리스트
        model.addAttribute("findKeyword", findKeyword); // 검색 키워드

        return "event/devEventList"; // 템플릿 반환
    }

    @PostMapping("/board/deleteEvent/{seasonId}")
    public String deleteEvent(@PathVariable("seasonId") int seasonId, Model model) {
        try {
            // 1. c_season과 연결된 board_id 조회
            int boardId = eventService.getBoardIdBySeasonId(seasonId);

            // 2. c_image 데이터 삭제
            List<ImageDTO> images = fileService.getImagesByBoardId(boardId);
            for (ImageDTO image : images) {
                String imagePath = "src/main/resources/static" + image.getImgPath();
                File targetImage = new File(imagePath);

                if (targetImage.exists()) {
                    targetImage.delete();
                }
            }
            fileService.deleteImagesByBoardId(boardId);

            // 3. c_file 데이터 삭제
            List<FileDTO> files = fileService.getFilesByBoardId(boardId);
            for (FileDTO file : files) {
                String filePath = "src/main/resources/static" + file.getFilePath();
                File targetFile = new File(filePath);

                if (targetFile.exists()) {
                    targetFile.delete();
                }
            }
            fileService.deleteFilesByBoardId(boardId);

            // 4. c_board 데이터 삭제
            boardService.deleteBoardById(boardId);

            // 5. c_season 데이터 삭제
            eventService.deleteSeason(seasonId);

            log.info("행사 및 관련 데이터 삭제 성공: 시즌 ID = {}", seasonId);
            return "redirect:/dev/board/eventList";
        } catch (Exception e) {
            log.error("행사 삭제 실패: 시즌 ID = {}, 에러: {}", seasonId, e.getMessage());
            model.addAttribute("errorMessage", "행사를 삭제하는 중 문제가 발생했습니다.");
            return "event/adminEventList";
        }
    }

    @GetMapping("/board/event/detail/{seasonId}")
    public String getEventDetail(@PathVariable("seasonId") int seasonId,
                                  @RequestParam(value = "companyId", required = false) Long companyId,
                                 Model model, HttpSession session) {
        // 시즌 데이터 가져오기
        SeasonDTO season = eventService.getSeasonById(seasonId);

        if (season == null) {
            throw new RuntimeException("Season 데이터를 찾을 수 없습니다: ID = " + seasonId);
        }
        // board_id 가져오기
        Integer boardId = boardMapper.getBoardIdBySeasonId(seasonId);
        if (boardId == null) {
            throw new RuntimeException("해당 seasonId에 연결된 boardId를 찾을 수 없습니다: " + seasonId);
        }

        // 조회수 증가
        eventService.increaseSeasonViews(seasonId);
        season.setSeasonViews(season.getSeasonViews() + 1); // 모델에 최신 조회수 반영

        // 로그인된 사용자 ID 가져오기
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        model.addAttribute("loginUser", loggedInUserId);

        // 댓글 데이터 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(boardId);
        model.addAttribute("commentList", commentList);

        // 이미지 및 텍스트 데이터 가져오기
        List<ImageDTO> contentList = fileService.getContentByBoardId(season.getBoardId());
        model.addAttribute("contentList", contentList);


        // 참여자 명단 가져오기
        // 참여자 명단 가져오기
        List<MemberDTO> attentionList = eventService.findAllAttentionMemberList(seasonId, companyId);

        // 모델에 데이터 추가
        model.addAttribute("season", season);
        model.addAttribute("attentionList", attentionList);
        log.info("===================={}", attentionList);
        log.info("Season 상세보기 데이터 == {}", season);

        return "event/devEventDetail"; // eventDetail.html로 이동
    }

    @GetMapping("/board/endevent/detail/{seasonId}")
    public String getEndEventDetail(@PathVariable("seasonId") int seasonId, Model model, HttpSession session) {
        // 시즌 데이터 가져오기
        SeasonDTO season = eventService.getSeasonById(seasonId);
        if (season == null) {
            throw new RuntimeException("Season 데이터를 찾을 수 없습니다: ID = " + seasonId);
        }

        // board_id 가져오기
        Integer boardId = boardMapper.getBoardIdBySeasonId(seasonId);
        if (boardId == null) {
            throw new RuntimeException("해당 seasonId에 연결된 boardId를 찾을 수 없습니다: " + seasonId);
        }

        // 시즌 상태 확인
        if (!"마감".equals(season.getSeasonState())) {
            throw new RuntimeException("해당 시즌은 마감되지 않았습니다: ID = " + seasonId);
        }

        // 조회수 증가
        eventService.increaseSeasonViews(seasonId);
        season.setSeasonViews(season.getSeasonViews() + 1); // 모델에 최신 조회수 반영

        //로그인된 사용자
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        model.addAttribute("loginUser", loggedInUserId);

        // 댓글 데이터 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(boardId);
        model.addAttribute("commentList", commentList);


        // 이미지 및 텍스트 데이터 가져오기
        List<ImageDTO> contentList = fileService.getContentByBoardId(season.getBoardId());

        // 모델에 시즌 데이터 추가
        model.addAttribute("season", season);
        model.addAttribute("contentList", contentList); // 이미지 및 텍스트 정보
        model.addAttribute("isDev", false); // 일반 유저

        return "event/devEventEndDetail"; // eventEndDetail.html로 이동
    }//------------------------------------

    @GetMapping("/board/editEvent/{seasonId}")
    public String editEventForm(@PathVariable("seasonId") int seasonId, Model model) {
        // 시즌 데이터 가져오기
        SeasonDTO season = eventService.getSeasonById(seasonId);
        if (season == null) {
            throw new RuntimeException("Season 데이터를 찾을 수 없습니다: ID = " + seasonId);
        }
        model.addAttribute("season", season);
        log.info("Season 데이터: {}", season); // season 데이터를 로그로 출력
        // 이벤트 리스트 가져오기
        List<EventDTO> eventList = eventService.getAllEvents();
        model.addAttribute("eventList", eventList);
        // 텍스트 데이터 가져오기
        List<ImageDTO> texts = fileService.getTextsByBoardId(season.getBoardId());
        model.addAttribute("texts", texts);
        // 이미지 데이터 가져오기
        List<ImageDTO> images = fileService.getImagesByBoardId(season.getBoardId());
        model.addAttribute("images", images);
        return "event/editEventForm";
    }


    @PostMapping("/board/updateEvent")
    public String updateEventPost(
            @ModelAttribute SeasonDTO seasonDTO,
            @RequestParam(value = "seasonId", required = false) Integer seasonId,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "texts[]", required = false) List<String> updatedTexts,
            @RequestParam(value = "textIds", required = false) List<Integer> textIds,
            @RequestParam(value = "deletedTextIds", required = false) List<Integer> deletedTextIds,
            @RequestParam(value = "newImages[]", required = false) MultipartFile[] newImages,
            @RequestParam(value = "deletedImageIds", required = false) List<Integer> deletedImageIds,
            Model model) {

        log.info("수신된 데이터 - Thumbnail: {}, File: {}, UpdatedTexts: {}, TextIds: {}, DeletedTextIds: {}, NewImages: {}, DeletedImageIds: {}",
                thumbnail, file, updatedTexts, textIds, deletedTextIds, newImages, deletedImageIds);
        log.info("Deleted Text IDs: {}", deletedTextIds);
        log.info("Deleted Image IDs: {}", deletedImageIds);



        // 1. 기존 시즌 데이터에서 round_number 가져오기
        SeasonDTO existingSeason = eventService.getSeasonById(seasonDTO.getSeasonId());
        if (existingSeason != null) {
            seasonDTO.setRoundNumber(existingSeason.getRoundNumber());
        }

        try {
            log.info("SeasonDTO 초기 상태: {}", seasonDTO);
            Integer boardId = eventService.getBoardIdBySeasonId(seasonDTO.getSeasonId());
            log.info("{}", thumbnail, file, updatedTexts, textIds, deletedTextIds, newImages, deletedImageIds);
            // 1. 데이터베이스에서 boardId 가져오기
            if (seasonDTO.getBoardId() == 0) {

                if (boardId != null) {
                    seasonDTO.setBoardId(boardId);
                } else {
                    log.warn("SeasonDTO의 boardId가 데이터베이스에서 조회되지 않았습니다.");
                }
            }

            // 4. 텍스트 삭제 처리
            if (deletedTextIds != null && !deletedTextIds.isEmpty()) {
                for (Integer textId : deletedTextIds) {
                    fileService.deleteTextById(textId); // 텍스트 삭제 서비스 호출
                    log.info("텍스트 삭제 성공: ID = {}", textId);
                }
            }

            // 5. 이미지 삭제 처리
            if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
                for (Integer imageId : deletedImageIds) {
                    fileService.deleteImageById(imageId); // 이미지 삭제 서비스 호출
                    log.info("이미지 삭제 성공: ID = {}", imageId);
                }
            }

            // 2. 상태 업데이트: 현재 날짜와 종료 날짜 비교
            LocalDate endDate = LocalDate.parse(seasonDTO.getSeasonEndDate());
            LocalDate today = LocalDate.now();
            if (!today.isBefore(endDate)) {
                seasonDTO.setSeasonState("마감");
            } else {
                seasonDTO.setSeasonState("모집중");
            }
            log.info("상태 업데이트 완료: {}", seasonDTO.getSeasonState());

            // 3. 썸네일 업로드 처리
            if (thumbnail != null && !thumbnail.isEmpty()) {
                deleteExistingFile(seasonDTO.getSeasonThumbnail());
                String thumbnailPath = saveFile(thumbnail, "thumbnails");
                seasonDTO.setSeasonThumbnail(thumbnailPath);
            }

            // 6. 텍스트 추가 및 수정
            if (updatedTexts != null && !updatedTexts.isEmpty()) {
                for (int i = 0; i < updatedTexts.size(); i++) {
                    String text = updatedTexts.get(i);
                    if (text != null && !text.isBlank()) {
                        ImageDTO textContent = new ImageDTO();
                        textContent.setBoardId(seasonDTO.getBoardId());
                        textContent.setContentType("text");
                        textContent.setContentOrder(i + 1); // 순서 부여
                        textContent.setContentData(text.trim());

                        if (textIds != null && i < textIds.size()) {
                            // 기존 텍스트 업데이트
                            textContent.setImgId(textIds.get(i));
                            fileService.updateContent(textContent);
                            log.info("텍스트 수정 성공: {}", textContent);
                        } else {
                            // 새로운 텍스트 추가
                            fileService.saveContent(textContent);
                            log.info("새 텍스트 추가 성공: {}", textContent);
                        }
                    }
                }
            }

            // 7. 새 이미지 추가
            if (newImages != null && newImages.length > 0) {
                log.info("이미지 추가 갯수: {}", newImages.length);
                int contentOrder = fileService.getMaxContentOrder(seasonDTO.getBoardId(), "image") + 1;
                for (MultipartFile image : newImages) {
                    if (image != null && !image.isEmpty()) {
                        String originalFilename = image.getOriginalFilename();
                        String fileName = UUID.randomUUID() + "_" + originalFilename;
                        String basePath = "src/main/resources/static/images";

                        try {
                            Path folderPath = Paths.get(basePath);
                            if (!Files.exists(folderPath)) {
                                Files.createDirectories(folderPath);
                            }
                            Path filePath = folderPath.resolve(fileName);
                            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                            ImageDTO imageContent = new ImageDTO();
                            imageContent.setBoardId(seasonDTO.getBoardId());
                            imageContent.setContentType("image");
                            imageContent.setContentOrder(contentOrder++);
                            imageContent.setImgPath("/images/" + fileName);
                            imageContent.setImgOriginName(originalFilename);
                            imageContent.setImgSize((int) image.getSize());
                            fileService.saveContent(imageContent);
                            log.info("새로운 이미지 추가 성공: {}", imageContent);
                        } catch (IOException e) {
                            log.error("이미지 저장 실패: {}", e.getMessage());
                            throw new RuntimeException("이미지 저장 실패", e);
                        }
                    }
                }
            }

            // 8. 시즌 정보 업데이트
            eventService.updateSeason(seasonDTO);

            // 9. c_board 테이블 업데이트
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

            return "redirect:/dev/board/eventList";
        } catch (Exception e) {
            log.error("Season 업데이트 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "수정 중 문제가 발생했습니다.");
            return "event/editEventForm";
        }
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

    @GetMapping("/board/event/getEventIdBySeason/{seasonId}")
    @ResponseBody
    public Map<String, Integer> getEventIdBySeason(@PathVariable("seasonId") int seasonId) {
        int eventId = eventService.getEventIdBySeasonId(seasonId);
        Map<String, Integer> response = new HashMap<>();
        response.put("eventId", eventId);
        return response;
    }

    @GetMapping("/board/event/seasons/{eventId}")
    @ResponseBody
    public List<SeasonDTO> getSeasonsByEventId(@PathVariable("eventId") int eventId) {
        return eventService.getSeasonsByEventId(eventId);
    }


}
