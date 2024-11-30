package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.*;
import com.kosmo.nexus.service.AdminService;
import com.kosmo.nexus.service.BoardService;
import com.kosmo.nexus.service.CommentService;
import com.kosmo.nexus.service.FileService;
import jakarta.servlet.ServletContext;
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
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
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
                                   Model model) {
        paging.setFindType(findType);
        paging.setFindKeyword(findKeyword);

        int notificationCount = boardService.getTotalNotificationCount(paging);
        int generalCount = boardService.getTotalGeneralCount(paging);
        int totalCount = notificationCount + generalCount;

        paging.setTotalCount(generalCount);
        paging.setOneRecordPage(10);
        paging.init();

        List<BoardDTO> list = boardService.selectNotificationList(paging);

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("notificationCount", notificationCount);
        model.addAttribute("generalCount", generalCount);
        model.addAttribute("notifications", list);
        model.addAttribute("paging", paging);

        return "/notice/notificationList";
    }

    // 공지사항 상세보기
    @GetMapping("/board/notificationDetail")
    public String notificationDetail(@RequestParam("num") int num, Model model) {
        boardService.increaseViewCount(num);

        // 공지사항 데이터 가져오기
        BoardDTO notification = boardService.findNotificationById(num);
        model.addAttribute("notification", notification);

        // 여러 첨부파일 데이터 가져오기
        List<FileDTO> attachedFiles = fileService.getFilesByBoardId(num);
        model.addAttribute("attachedFiles", attachedFiles);

        log.info("첨부파일 목록 데이터: {}", attachedFiles);

        // 댓글 데이터 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(num);
        model.addAttribute("commentList", commentList);

        log.info("공지사항 상세보기 데이터 == {}", notification);
        log.info("첨부파일 목록 데이터 == {}", attachedFiles);
        log.info("댓글 목록 데이터 == {}", commentList);

        return "notice/notificationDetail";
    }

    // 공지사항 작성 폼
    @GetMapping("/board/notification")
    public String notificationForm() {
        return "notice/notification";
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
        Enumeration<String> attributeNames = session.getAttributeNames();
        log.info("등록확인");
        log.info("test==={}", attributeNames);
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            log.info("세션 속성: {} = {}",attributeName, session.getAttribute(attributeName));
        }

        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        log.info("로그인된 아이디 ========{}", loggedInUserId);
        noticeDTO.setMemberId(loggedInUserId);
        noticeDTO.setBoardCategory("공지사항");

        log.info("공지사항 추가 요청 데이터: {}", noticeDTO);

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

        return "redirect:/board/notificationList";
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
        return "notice/notificationEdit";
    }


    @PostMapping("/board/editNotification")
    public String updateNotification(
            @ModelAttribute NoticeDTO noticeDTO,
            @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles) {
        log.info("수정 요청 데이터: {}", noticeDTO);

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
        return "redirect:/board/notificationDetail?num=" + noticeDTO.getBoardId();
    }

    @DeleteMapping("/board/deleteNotification")
    public ResponseEntity<String> deleteNotification(@RequestParam("num") int num, HttpSession session) {
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        try {
            // 권한 확인
            if (!"testUser".equals(loggedInUserId)) {
                log.warn("삭제 권한 없음: 사용자 ID = {}", loggedInUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
            }

            // 첨부파일 삭제
            fileService.deleteFilesByBoardId(num);

            // 댓글 삭제
            commentService.deleteCommentsByBoardId(num);

            // 게시물 삭제
            boardService.deleteNotification(num);

            log.info("게시물 삭제 성공: ID = {}", num);
            return ResponseEntity.ok("게시물이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("게시물 삭제 실패: {}", e.getMessage());
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
            log.info("댓글/대댓글 작성 성공: {}", comment);
        } catch (Exception e) {
            log.error("댓글/대댓글 작성 실패: {}", e.getMessage());
        }//----------------------------------

        // 저장 후 상세보기 페이지로 리다이렉트
        return "redirect:/board/notificationDetail?num=" + comment.getBoardId();
    }//----------------------------------------------------

    @PostMapping("/board/deleteComment")
    public String deleteComment(@RequestParam Long commentId,
                                @RequestParam String memberId,
                                @RequestParam int boardId,
                                Model model,
                                HttpSession session) {
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        try {
            // 댓글 삭제 서비스 호출
            commentService.deleteCommentAndReplies(commentId, loggedInUserId);
            log.info("댓글 삭제 성공: {}", commentId);

            // 삭제 성공 후 상세보기 페이지로 리다이렉트
            return "redirect:/board/notificationDetail?num=" + boardId;
        } catch (IllegalAccessException e) {
            log.error("댓글 삭제 실패 - 권한 없음: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 권한 없음");
            return "redirect:/board/notificationDetail?num=" + boardId;
        } catch (Exception e) {
            log.error("댓글 삭제 실패 - 서버 오류: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 서버 오류");
            return "redirect:/board/notificationDetail?num=" + boardId;
        }
    }//---------------------------------

    @PostMapping("/board/editComment")
    public String editComment(@RequestParam("commentId") Long commentId,
                              @RequestParam("commentContent") String commentContent,
                              @RequestParam("boardId") int boardId,
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
            log.info("댓글 수정 성공: {}", comment);

            return "redirect:/board/notificationDetail?num=" + boardId; // 댓글이 속한 공지로 리다이렉트
        } catch (IllegalAccessException e) {
            log.error("댓글 수정 실패: 권한 없음", e);
            model.addAttribute("alertMessage", "수정 실패: 권한이 없습니다.");
            return "redirect:/board/notificationDetail?num=" + commentId; // 실패 시에도 같은 페이지로 리다이렉트
        } catch (Exception e) {
            log.error("댓글 수정 실패: 서버 오류", e);
            model.addAttribute("alertMessage", "수정 실패: 서버 오류");
            return "redirect:/board/notificationDetail?num=" + commentId;
        }
    }
    //--------------------------------------------------------------------------------------------------------
    // QnA 목록 페이지 반환
    @GetMapping("/board/qnaList")
    public String qnaList(
            @RequestParam(defaultValue = "title") String findType, // 검색 유형 (기본값: 제목)
            @RequestParam(required = false) String findKeyword, // 검색 키워드
            @RequestParam(defaultValue = "1") int page, // 현재 페이지 (기본값: 1)
            @RequestParam(defaultValue = "10") int pageSize, // 페이지당 게시글 수 (기본값: 10)
            Model model) {
        // 페이징 DTO 설정
        PagingDTO paging = new PagingDTO();
        paging.setFindType(findType);
        paging.setFindKeyword(findKeyword);
        paging.setPageNum(page);
        paging.setOneRecordPage(pageSize);

        // 전체 게시글 수 및 페이징 계산
        int totalCount = boardService.getTotalQnaCount(paging); // QnA 게시글 총 개수 조회
        paging.setTotalCount(totalCount);
        paging.init();

        // Q&A 게시글 목록 조회
        List<BoardDTO> qnaList = boardService.selectQnaList(paging);

        // 모델에 데이터 전달
        model.addAttribute("qnaList", qnaList); // Q&A 게시글 리스트
        model.addAttribute("paging", paging); // 페이징 데이터
        model.addAttribute("findType", findType); // 검색 유형
        model.addAttribute("findKeyword", findKeyword); // 검색 키워드

        return "qna/qnaList"; // Q&A 목록 페이지로 이동
    }

    // QnA 상세보기
    @GetMapping("/board/qnaDetail")
    public String qnaDetail(@RequestParam("num") int num, Model model, HttpSession session) {
        // 조회수 증가
        boardService.increaseQnaViewCount(num);

        // 로그인된 사용자 ID 가져오기
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        model.addAttribute("loggedInUserId", loggedInUserId);

        // QnA 데이터 가져오기
        BoardDTO qna = boardService.findQnaById(num);
        model.addAttribute("qna", qna);

        // 댓글 및 대댓글 데이터 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(num);
        model.addAttribute("commentList", commentList);

        log.info("QnA 상세보기 데이터 == {}", qna);
        log.info("댓글 목록 데이터 == {}", commentList);

        return "qna/qnaDetail";
    }

    // QnA 수정 폼 반환
    @GetMapping("/board/editQna")
    public String editQna(@RequestParam("num") int num, Model model) {
        // Q&A 데이터 가져오기
        BoardDTO qna = boardService.findQnaById(num);
        model.addAttribute("qna", qna);

        return "qna/qnaEdit"; // 수정 페이지 경로
    }

    // QnA 수정 처리
    @PostMapping("/board/editQna")
    public String updateQna(@ModelAttribute BoardDTO boardDTO) {
        // Q&A 수정 처리
        boardService.updateQna(boardDTO);

        return "redirect:/board/qnaDetail?num=" + boardDTO.getBoardId();
    }

    @PostMapping("/board/addQnaComment")
    public String addQnaComment(@ModelAttribute CommentDTO comment, HttpSession session) {
        // 로그인된 사용자 ID 설정
        String memberId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        comment.setMemberId(memberId);

        try {
            // 댓글 또는 대댓글 저장
            commentService.saveComment(comment);
            log.info("QnA 댓글/대댓글 작성 성공: {}", comment);
        } catch (Exception e) {
            log.error("QnA 댓글/대댓글 작성 실패: {}", e.getMessage());
        }

        // 저장 후 QnA 상세보기 페이지로 리다이렉트
        return "redirect:/board/qnaDetail?num=" + comment.getBoardId();
    }

    @PostMapping("/board/deleteQnaComment")
    public String deleteQnaComment(@RequestParam Long commentId,
                                   @RequestParam int boardId,
                                   HttpSession session, Model model) {
        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();

        try {
            // 댓글 삭제 서비스 호출
            commentService.deleteCommentAndReplies(commentId, loggedInUserId);
            log.info("QnA 댓글 삭제 성공: {}", commentId);
            return "redirect:/board/qnaDetail?num=" + boardId;
        } catch (IllegalAccessException e) {
            log.error("QnA 댓글 삭제 실패 - 권한 없음: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 권한 없음");
            return "redirect:/board/qnaDetail?num=" + boardId;
        } catch (Exception e) {
            log.error("QnA 댓글 삭제 실패 - 서버 오류: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 서버 오류");
            return "redirect:/board/qnaDetail?num=" + boardId;
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
            log.info("QnA 댓글 수정 성공: {}", comment);

            return "redirect:/board/qnaDetail?num=" + boardId; // 댓글이 속한 QnA로 리다이렉트
        } catch (IllegalAccessException e) {
            log.error("QnA 댓글 수정 실패: 권한 없음", e);
            model.addAttribute("alertMessage", "수정 실패: 권한이 없습니다.");
            return "redirect:/board/qnaDetail?num=" + boardId; // 실패 시에도 같은 페이지로 리다이렉트
        } catch (Exception e) {
            log.error("QnA 댓글 수정 실패: 서버 오류", e);
            model.addAttribute("alertMessage", "수정 실패: 서버 오류");
            return "redirect:/board/qnaDetail?num=" + boardId;
        }
    }
    @PostMapping("/board/deleteQna")
    public String deleteQna(@RequestParam("boardId") int boardId, HttpSession session, Model model) {
        try {
            // QnA 삭제 처리
            String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
            boardService.deleteQna(boardId);
            log.info("QnA 삭제 성공: {}", boardId);

            return "redirect:/board/qnaList"; // 삭제 후 QnA 목록으로 리다이렉트
        } catch (Exception e) {
            log.error("QnA 삭제 실패", e);
            model.addAttribute("alertMessage", "QnA 삭제에 실패했습니다.");
            return "redirect:/board/qnaDetail?num=" + boardId; // 실패 시 해당 QnA 상세 페이지로 리다이렉트
        }
    }

}


