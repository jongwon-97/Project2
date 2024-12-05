package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.*;
import com.kosmo.nexus.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Controller
@Slf4j
@RequestMapping("/admin")
public class AdminBoardController {

    @Autowired
    private BoardService boardService;
    @Autowired
    private FileService fileService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private EventService eventService;


    private String saveFileWithUUID(MultipartFile file, String uploadDir) throws IOException {
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path destination = path.resolve(fileName);
        Files.copy(file.getInputStream(), destination);

        return "/static/upload/" + fileName;
    }

    // 공지사항 목록
    @GetMapping("/board/notificationList")
    public String notificationList(@RequestParam(defaultValue = "5") int blockSize,
                                   @RequestParam(required = false) String findType,
                                   @RequestParam(required = false) String findKeyword,
                                   PagingDTO paging,
                                   HttpSession session,
                                   Model model) {
        paging.setFindType(findType);
        paging.setFindKeyword(findKeyword);
        Long companyId = ((LoginDTO) session.getAttribute("loginUser")).getCompanyId();

        int notificationCount = boardService.getTotalNotificationCount(paging);
        int generalCount = boardService.getTotalCompanyCount(paging, companyId);
//        log.info("paging, companyID===={}, {}",paging, companyId);
//        int totalCount = notificationCount + generalCount;
//        log.info("총 게시물 개수 ====={}", generalCount);
        paging.setTotalCount(generalCount);
        paging.setOneRecordPage(10);
        paging.init();

        List<BoardDTO> list = boardService.selectNotificationListByCompanyId(paging, companyId);

        //model.addAttribute("totalCount", totalCount);
        model.addAttribute("notificationCount", notificationCount);
        model.addAttribute("generalCount", generalCount);
        model.addAttribute("notifications", list);
        model.addAttribute("paging", paging);


        //model.addAttribute("content", "notice/adminNotificationList :: content");
        //return "sidebar";

        return "notice/adminNotificationList";
    }

    // 공지사항 상세보기
    @GetMapping("/board/notificationDetail")
    public String notificationDetail(@RequestParam("num") int num, Model model,
                                     HttpSession session) {

        // 공지사항 데이터 가져오기
        BoardDTO notification = boardService.findNotificationById(num);

        // 작성자의 회사iD와 user의 회사ID를 비교하여, 일치하지 않으면 접근 제한
        String boardMemberId = notification.getMemberId();
        Long logCompanyId = ((LoginDTO) session.getAttribute("loginUser")).getCompanyId();
        Long boardCompanyId = adminService.findCompanyIdByMemberId(boardMemberId);
        if (!logCompanyId.equals(boardCompanyId) && !boardCompanyId.equals(0L)){
            String msg = "접근 권한이 없는 게시글 입니다.";
            String loc = "/admin/board/notificationList";
            return message(model, msg, loc);
        }
        boardService.increaseViewCount(num);
        notification = boardService.findNotificationById(num);

        model.addAttribute("notification", notification);

        // 여러 첨부파일 데이터 가져오기
        List<FileDTO> attachedFiles = fileService.getFilesByBoardId(num);
        model.addAttribute("attachedFiles", attachedFiles);

        //log.info("첨부파일 목록 데이터: {}", attachedFiles);

        // 댓글 데이터 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(num);
        model.addAttribute("commentList", commentList);
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        model.addAttribute("loginUser", loggedInUserId);


        return "notice/adminNotificationDetail";
    }

    // 공지사항 작성 폼
    @GetMapping("/board/notification")
    public String notificationForm() {
        return "notice/adminNotification";
    }

    // 공지사항 작성
    @PostMapping("/board/notification")
    public String addNotification(@ModelAttribute NoticeDTO noticeDTO,
                                  @RequestParam(value = "files", required = false) MultipartFile[] files,
                                  HttpSession session,
                                  Model model) { // HttpSession 추가
        if (noticeDTO.getIsNotice() == null) {
            noticeDTO.setIsNotice(false);
        }

//        Enumeration<String> attributeNames = session.getAttributeNames();
        //log.info("등록확인");
        //log.info("test==={}", attributeNames);
//        while (attributeNames.hasMoreElements()) {
//            String attributeName = attributeNames.nextElement();
//            //log.info("세션 속성: {} = {}",attributeName, session.getAttribute(attributeName));
//        }

        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        //log.info("로그인된 아이디 ========{}", loggedInUserId);
        noticeDTO.setMemberId(loggedInUserId);
        noticeDTO.setBoardCategory("공지사항");

        //log.info("공지사항 추가 요청 데이터: {}", noticeDTO);

        boardService.insertNotification(noticeDTO);

        int boardId = noticeDTO.getBoardId();
        if (files != null && files.length > 0) {
            String uploadDir = new File("src/main/resources/static/upload").getAbsolutePath();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String savedPath = saveFileWithUUID(file, uploadDir);

                        FileDTO fileDTO = new FileDTO();
                        fileDTO.setFilePath(savedPath);
                        fileDTO.setBoardId(boardId);
                        fileDTO.setFileSize(file.getSize());
                        fileDTO.setFileOriginName(file.getOriginalFilename());

                        fileService.saveFile(fileDTO);
                    } catch (IOException e) {
                        log.error("파일 저장 실패: {}", e.getMessage());
                    }
                }
            }
        }
        return "redirect:/admin/board/notificationList";
    }

    @GetMapping("/board/editNotification")
    public String editNotification(@RequestParam("num") int num, Model model) {
        // 공지사항 정보 조회
        BoardDTO notification = boardService.findNotificationById(num);
        model.addAttribute("notification", notification);

        // 첨부파일 정보 조회
        List<FileDTO> attachedFiles = fileService.getFilesByBoardId(num);
        model.addAttribute("attachedFiles", attachedFiles);

        // 수정 폼으로 이동
        return "notice/adminNotificationEdit";
    }


    @PostMapping("/board/editNotification")
    public String updateNotification(
            @ModelAttribute NoticeDTO noticeDTO,
            @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles) {
        //log.info("수정 요청 데이터: {}", noticeDTO);

        // 기본값 설정
        if (noticeDTO.getIsNotice() == null) {
            noticeDTO.setIsNotice(false); // 기본값 설정
        }

        // 공지사항 수정
        boardService.updateNotification(noticeDTO);

        // 새로운 파일 업로드 처리
        if (newFiles != null && !newFiles.isEmpty()) {
            String uploadPath = new File("src/main/resources/static/upload").getAbsolutePath();

            for (MultipartFile newFile : newFiles) {
                if (!newFile.isEmpty()) {
                    String fileName = newFile.getOriginalFilename();
                    try {
                        // 파일 저장
                        File saveFile = new File(uploadPath, fileName);
                        if (!saveFile.getParentFile().exists()) {
                            saveFile.getParentFile().mkdirs();
                        }
                        newFile.transferTo(saveFile);

                        // 파일 정보 저장
                        FileDTO fileDTO = new FileDTO();
                        fileDTO.setFilePath("/upload/" + fileName); // 정적 리소스 경로
                        fileDTO.setBoardId(noticeDTO.getBoardId()); // 게시글 ID 연결
                        fileDTO.setFileSize(newFile.getSize());
                        fileDTO.setFileOriginName(fileName);

                        // 파일 저장
                        fileService.saveFile(fileDTO);
                    } catch (IOException e) {
                        log.error("파일 저장 실패: {}", e.getMessage());
                    }
                }
            }
        }

        // 수정 후 상세보기 페이지로 리다이렉트
        return "redirect:/admin/board/notificationDetail?num=" + noticeDTO.getBoardId();
    }

    @DeleteMapping("/board/deleteNotification")
    public ResponseEntity<String> deleteNotification(@RequestParam("num") int num, HttpSession session) {
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        // 공지사항 번호로 작성자 정보를 가져오기
        BoardDTO notification = boardService.findNotificationById(num);
        String notiMemberId = notification.getMemberId();
        try {
            // 권한 확인
            if (!notiMemberId.equals(loggedInUserId)) {
                log.warn("삭제 권한 없음: 사용자 ID = {}", loggedInUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
            }

            // 첨부파일 삭제
            fileService.deleteFilesByBoardId(num);

            // 댓글 삭제
            commentService.deleteCommentsByBoardId(num);

            // 게시물 삭제
            boardService.deleteNotification(num);

            //log.info("게시물 삭제 성공: ID = {}", num);
            return ResponseEntity.ok("게시물이 삭제되었습니다.");
        } catch (Exception e) {
            //log.error("게시물 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시물 삭제에 실패했습니다.");
        }
    }


    // 파일 다운로드
    @GetMapping("/board/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filePath") String filePath) {
        try {
            // '/static' 제거 및 경로 설정
            String sanitizedPath = filePath.replaceFirst("/static", "");
            Path path = Paths.get("src/main/resources/static" + sanitizedPath);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // 원본 파일명 추출 (UUID 제거)
            String originalFileName = path.getFileName().toString();
            originalFileName = originalFileName.substring(originalFileName.indexOf("_") + 1); // UUID 제거

            // 파일 이름을 UTF-8로 인코딩
            String encodedFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);

            // MIME 타입 설정
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 파일 다운로드 응답 반환
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 댓글 작성
    @PostMapping("/board/addComment")
    public String addComment(@ModelAttribute CommentDTO comment, HttpSession session) {
        // 로그인된 사용자 ID 설정
        String memberId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        comment.setMemberId(memberId);

        try {
            // 댓글 또는 대댓글 저장
            commentService.saveComment(comment);
            //log.info("댓글/대댓글 작성 성공: {}", comment);
        } catch (Exception e) {
            //log.error("댓글/대댓글 작성 실패: {}", e.getMessage());
        }//----------------------------------

        // 저장 후 상세보기 페이지로 리다이렉트
        return "redirect:/admin/board/notificationDetail?num=" + comment.getBoardId();
    }//----------------------------------------------------

    @PostMapping("/board/deleteComment")
    public String deleteComment(@RequestParam Long commentId,
                                @RequestParam(value = "boardId", required = false) Integer boardId,
                                Model model,
                                HttpSession session) {
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        try {
            // 댓글 삭제 서비스 호출
            commentService.deleteCommentAndReplies(commentId, loggedInUserId);
            //log.info("댓글 삭제 성공: {}", commentId);

            // 삭제 성공 후 상세보기 페이지로 리다이렉트
            return "redirect:/admin/board/notificationDetail?num=" + boardId;
        } catch (IllegalAccessException e) {
            log.error("댓글 삭제 실패 - 권한 없음: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 권한 없음");
            return "redirect:/admin/board/notificationDetail?num=" + boardId;
        } catch (Exception e) {
            log.error("댓글 삭제 실패 - 서버 오류: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 서버 오류");
            return "redirect:/admin/board/notificationDetail?num=" + boardId;
        }
    }//---------------------------------

    @PostMapping("/board/editComment")
    public String editComment(@RequestParam("commentId") Long commentId,
                              @RequestParam("commentContent") String commentContent,
                              @RequestParam(value = "boardId", required = false) Integer boardId,
                              HttpSession session, Model model) {
        String memberId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        try {
            // 댓글 수정 처리
            CommentDTO comment = new CommentDTO();
            comment.setCommentId(commentId);
            comment.setCommentContent(commentContent);
            comment.setBoardId(boardId); // 공지사항 ID 추가
            comment.setMemberId(memberId);

            commentService.updateComment(comment, memberId);
            //log.info("댓글 수정 성공: {}", comment);

            return "redirect:/admin/board/notificationDetail?num=" + boardId; // 댓글이 속한 공지로 리다이렉트
        } catch (IllegalAccessException e) {
            //log.error("댓글 수정 실패: 권한 없음", e);
            model.addAttribute("alertMessage", "수정 실패: 권한이 없습니다.");
            return "redirect:/admin/board/notificationDetail?num=" + commentId; // 실패 시에도 같은 페이지로 리다이렉트
        } catch (Exception e) {
            //log.error("댓글 수정 실패: 서버 오류", e);
            model.addAttribute("alertMessage", "수정 실패: 서버 오류");
            return "redirect:/admin/board/notificationDetail?num=" + commentId;
        }
    }
    //--------------------------------------------------------------------------------------------------------
    // QnA 목록 페이지 반환
    @GetMapping("/board/qnaList")
    public String qnaList(
            @RequestParam(required = false) String findType, // 검색 유형 (기본값: 제목)
            @RequestParam(required = false) String findKeyword, // 검색 키워드
            HttpSession session,
            PagingDTO paging,
            Model model) {
        // 페이징 DTO 설정
        paging.setFindType(findType);
        paging.setFindKeyword(findKeyword);
        paging.setOneRecordPage(10);
        Long companyId = ((LoginDTO) session.getAttribute("loginUser")).getCompanyId();
        // 전체 게시글 수 및 페이징 계산
        int totalCount = boardService.getTotalQnaCompanyCount(paging, companyId); // QnA 게시글 총 개수 조회
        paging.setTotalCount(totalCount);
        paging.init();

        // Q&A 게시글 목록 조회
//        List<BoardDTO> qnaList = boardService.selectQnaList(paging);
        List<BoardDTO> qnaList = boardService.selectQnaListByCompanyID(paging, companyId);


        // 모델에 데이터 전달
        model.addAttribute("qnaList", qnaList); // Q&A 게시글 리스트
        model.addAttribute("paging", paging); // 페이징 데이터
        model.addAttribute("findType", findType); // 검색 유형
        model.addAttribute("findKeyword", findKeyword); // 검색 키워드

        return "qna/adminQnaList"; // Q&A 목록 페이지로 이동
    }

    // QnA 상세보기
    @GetMapping("/board/qnaDetail")
    public String qnaDetail(@RequestParam("num") int num, Model model, HttpSession session) {

        BoardDTO qna = boardService.findQnaById(num);

        String boardMemberId = qna.getMemberId();
        Long logCompanyId = ((LoginDTO) session.getAttribute("loginUser")).getCompanyId();
        Long boardCompanyID = adminService.findCompanyIdByMemberId(boardMemberId);
        if (!logCompanyId.equals(boardCompanyID)){
            String msg = "접근 권한이 없는 게시글 입니다.";
            String loc = "/admin/board/qnaList";
            return message(model, msg, loc);
        }
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        // 조회수 증가
        boardService.increaseQnaViewCount(num);
        qna = boardService.findQnaById(num);
        // 댓글 및 대댓글 데이터 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(num);

        model.addAttribute("qna", qna);
        model.addAttribute("commentList", commentList);
        model.addAttribute("loginUser", loggedInUserId);
        model.addAttribute("qnaCompanyID", boardCompanyID);
        model.addAttribute("logCompanyId", logCompanyId);

        return "qna/adminQnaDetail";
    }

    @GetMapping("/board/qna")
    public String goToQnaForm(){
        return "qna/adminQna";
    }

    @PostMapping("/board/qna")
    public String insertQna(@ModelAttribute BoardDTO boardDTO,
                            HttpSession ses){
        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();
        boardDTO.setMemberId(sesLoginId);
        boardService.insertQna(boardDTO);
        return "redirect:/admin/board/qnaList";
    }

    // QnA 수정 폼 반환
    @GetMapping("/board/editQna")
    public String editQna(@RequestParam("num") int num, Model model) {
        // Q&A 데이터 가져오기
        BoardDTO qna = boardService.findQnaById(num);
        model.addAttribute("qna", qna);

        return "qna/adminQnaEdit"; // 수정 페이지 경로
    }

    // QnA 수정 처리
    @PostMapping("/board/editQna")
    public String updateQna(@ModelAttribute BoardDTO boardDTO) {
        // Q&A 수정 처리
        boardService.updateQna(boardDTO);

        return "redirect:/admin/board/qnaDetail?num=" + boardDTO.getBoardId();
    }

    @PostMapping("/board/addQnaComment")
    public String addQnaComment(@ModelAttribute CommentDTO comment, HttpSession session) {
        // 로그인된 사용자 ID 설정
        String memberId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        comment.setMemberId(memberId);

        try {
            // 댓글 또는 대댓글 저장
            commentService.saveComment(comment);
            //log.info("QnA 댓글/대댓글 작성 성공: {}", comment);
        } catch (Exception e) {
            //log.error("QnA 댓글/대댓글 작성 실패: {}", e.getMessage());
        }

        // 저장 후 QnA 상세보기 페이지로 리다이렉트
        return "redirect:/admin/board/qnaDetail?num=" + comment.getBoardId();
    }

    @PostMapping("/board/deleteQnaComment")
    public String deleteQnaComment(@RequestParam Long commentId,
                                   @RequestParam int boardId,
                                   HttpSession session, Model model) {
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        try {
            // 댓글 삭제 서비스 호출
            commentService.deleteCommentAndReplies(commentId, loggedInUserId);
            //log.info("QnA 댓글 삭제 성공: {}", commentId);
            return "redirect:/admin/board/qnaDetail?num=" + boardId;
        } catch (IllegalAccessException e) {
            //log.error("QnA 댓글 삭제 실패 - 권한 없음: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 권한 없음");
            return "redirect:/admin/board/qnaDetail?num=" + boardId;
        } catch (Exception e) {
            //log.error("QnA 댓글 삭제 실패 - 서버 오류: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 서버 오류");
            return "redirect:/admin/board/qnaDetail?num=" + boardId;
        }
    }

    @PostMapping("/board/editQnaComment")
    public String editQnaComment(@RequestParam("commentId") Long commentId,
                                 @RequestParam("commentContent") String commentContent,
                                 @RequestParam("boardId") int boardId,
                                 HttpSession session, Model model) {
        String memberId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        try {
            // 댓글 수정 처리
            CommentDTO comment = new CommentDTO();
            comment.setCommentId(commentId);
            comment.setCommentContent(commentContent);
            comment.setBoardId(boardId); // QnA ID 추가
            comment.setMemberId(memberId);

            commentService.updateComment(comment, memberId);
            //log.info("QnA 댓글 수정 성공: {}", comment);

            return "redirect:/admin/board/qnaDetail?num=" + boardId; // 댓글이 속한 QnA로 리다이렉트
        } catch (IllegalAccessException e) {
            //log.error("QnA 댓글 수정 실패: 권한 없음", e);
            model.addAttribute("alertMessage", "수정 실패: 권한이 없습니다.");
            return "redirect:/admin/board/qnaDetail?num=" + boardId; // 실패 시에도 같은 페이지로 리다이렉트
        } catch (Exception e) {
            //log.error("QnA 댓글 수정 실패: 서버 오류", e);
            model.addAttribute("alertMessage", "수정 실패: 서버 오류");
            return "redirect:/admin/board/qnaDetail?num=" + boardId;
        }
    }
    @PostMapping("/board/deleteQna")
    public String deleteQna(@RequestParam("boardId") int boardId, HttpSession session, Model model) {
        try {
            // QnA 삭제 처리
            //String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
            Long loggedInCompanyId = ((LoginDTO) session.getAttribute("loginUser")).getCompanyId();

            // 로그인된 companyID와 작성자의 companyID가 같을 때만 삭제함.
            BoardDTO boardDTO = boardService.findQnaById(boardId);
            Long boardCompanyId= adminService.findCompanyIdByMemberId(boardDTO.getMemberId());
            if(!boardCompanyId.equals(loggedInCompanyId)){
                String msg = "삭제 권한이 없는 게시글 입니다.";
                String loc = "/admin/board/qnaList";
                return message(model, msg, loc);
            }
            boardService.deleteQna(boardId);
            //log.info("QnA 삭제 성공: {}", boardId);

            return "redirect:/admin/board/qnaList"; // 삭제 후 QnA 목록으로 리다이렉트
        } catch (Exception e) {
            //log.error("QnA 삭제 실패", e);
            model.addAttribute("alertMessage", "QnA 삭제에 실패했습니다.");
            return "redirect:/admin/board/qnaDetail?num=" + boardId; // 실패 시 해당 QnA 상세 페이지로 리다이렉트
        }
    }


    @PostMapping("/board/addCommentBySeason")
    public String addCommentBySeason(@RequestParam("seasonId") int seasonId,
                                     @RequestParam("commentContent") String commentContent,
                                     @RequestParam(value = "parentId", required = false) Long parentId,
                                     HttpSession session) {
        // 로그인된 사용자 ID 가져오기
        String memberId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        if (memberId == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        // season_id로 board_id 조회
        int boardId = eventService.getBoardIdBySeasonId(seasonId);
        //log.info("조회된 boardId: {}", boardId);

        // 댓글 DTO 생성
        CommentDTO comment = new CommentDTO();
        comment.setBoardId(boardId); // board_id 설정
        comment.setMemberId(memberId);
        comment.setCommentContent(commentContent);
        comment.setParentId(parentId); // 대댓글의 경우 parentId 설정

        // 댓글 저장
        commentService.saveComment(comment);
        //log.info("댓글 저장 완료: {}", comment);

        // 댓글 작성 후 상세보기로 리다이렉트
        return "redirect:/admin/board/event/detail/" + seasonId + "#commentsSection";
    }



    public String message(Model model, String msg, String loc){
        model.addAttribute("msg", msg);
        model.addAttribute("loc", loc);
        return "message";
    }

    @PostMapping("/board/deleteEventComment")
    public String deleteEventComment(@RequestParam("commentId") Long commentId,
                                     @RequestParam("seasonId") int seasonId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        try {
            // 댓글 삭제 로직 호출
            commentService.deleteCommentAndReplies(commentId, loggedInUserId);
            redirectAttributes.addFlashAttribute("alertMessage", "댓글이 성공적으로 삭제되었습니다.");
        } catch (IllegalAccessException e) {
            redirectAttributes.addFlashAttribute("alertMessage", "댓글 삭제 실패: 권한이 없습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alertMessage", "댓글 삭제 실패: 서버 오류가 발생했습니다.");
        }


        // 시즌 상세 페이지로 리다이렉트
        return "redirect:/admin/board/event/detail/" + seasonId;
    }

    @PostMapping("/board/editEventComment")
    public String editEventComment(@RequestParam("commentId") Long commentId,
                                   @RequestParam("commentContent") String commentContent,
                                   @RequestParam("seasonId") int seasonId, // seasonId를 사용
                                   HttpSession session, Model model) {
        String memberId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        try {
            // 댓글 수정 처리
            CommentDTO comment = new CommentDTO();
            comment.setCommentId(commentId);
            comment.setCommentContent(commentContent);
            comment.setSeasonId(seasonId); // season_id 설정
            comment.setMemberId(memberId);

            commentService.updateComment(comment, memberId);
            log.info("댓글 수정 성공: {}", comment);

            // 성공적으로 수정된 후 해당 시즌 상세 페이지로 리다이렉트
            return "redirect:/admin/board/event/detail/" + seasonId;
        } catch (IllegalAccessException e) {
            log.error("댓글 수정 실패: 권한 없음", e);
            model.addAttribute("alertMessage", "수정 실패: 권한이 없습니다.");
            // 실패 시에도 같은 시즌 상세 페이지로 리다이렉트
            return "redirect:/admin/board/event/detail/" + seasonId;
        } catch (Exception e) {
            log.error("댓글 수정 실패: 서버 오류", e);
            model.addAttribute("alertMessage", "수정 실패: 서버 오류");
            // 서버 오류 시에도 같은 시즌 상세 페이지로 리다이렉트
            return "redirect:/admin/board/event/detail/" + seasonId;
        }
    }

}

