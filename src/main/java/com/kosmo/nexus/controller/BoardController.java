package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.*;
import com.kosmo.nexus.service.BoardService;
import com.kosmo.nexus.service.CommentService;
import com.kosmo.nexus.service.FileService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
public class BoardController {

    @Autowired
    private BoardService boardService;
    @Autowired
    private FileService fileService; // FileService 주입
    @Autowired
    private CommentService commentService;

    // 공통 메서드: 로그인된 사용자 ID 가져오기
    private String getLoggedInUserId(HttpSession session) {
        String loggedInUserId = (String) session.getAttribute("loggedInUserId");
        return (loggedInUserId != null) ? loggedInUserId : "testUser"; // 로그인하지 않은 경우 testUser 사용
    }

    // 공지사항 목록 페이지 반환
    @GetMapping("/board/notificationList")
    public String notificationList(@RequestParam(defaultValue = "5") int blockSize,
                                   @RequestParam(required = false) String findType, // 검색 타입 (title, member)
                                   @RequestParam(required = false) String findKeyword, // 검색 키워드
                                   PagingDTO paging,
                                   Model model) {
        // 검색 조건 설정
        paging.setFindType(findType);
        paging.setFindKeyword(findKeyword);

        // 공지사항과 일반 글의 개수 구하기
        int notificationCount = boardService.getTotalNotificationCount(paging); // 공지사항 수 구하기
        int generalCount = boardService.getTotalGeneralCount(paging); // 일반 글 수 구하기
        int totalCount = notificationCount + generalCount;

        paging.setTotalCount(generalCount);
        paging.setOneRecordPage(10);
        paging.init();

        //게시물 목록 조회
        List<BoardDTO> list = boardService.selectNotificationList(paging);

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("notificationCount", notificationCount);
        model.addAttribute("generalCount", generalCount);
        model.addAttribute("notifications", list);
        model.addAttribute("paging", paging);

        return "notice/notificationList";
    }

    // 공지사항 상세보기
    @GetMapping("/board/notificationDetail")
    public String notificationDetail(@RequestParam("num") int num, Model model) {
        // 조회수 증가
        boardService.increaseViewCount(num);

        // 공지사항 데이터 가져오기
        BoardDTO notification = boardService.findNotificationById(num);
        model.addAttribute("notification", notification);

        // 첨부파일 데이터 가져오기
        FileDTO attachedFile = fileService.getFileByBoardId(num);
        model.addAttribute("attachedFile", attachedFile);

        // 댓글 및 대댓글 데이터 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(num);
        model.addAttribute("commentList", commentList);

        log.info("공지사항 상세보기 데이터 == {}", notification);
        log.info("첨부파일 데이터 == {}", attachedFile);
        log.info("댓글 목록 데이터 == {}", commentList);

        return "notice/notificationDetail";
    }

    // 공지사항 작성 폼 반환
    @GetMapping("/board/notification")
    public String notificationForm() {
        return "notice/notification"; // 변경된 뷰 경로
    }

    // 공지사항 작성 처리
    @PostMapping("/board/notification")
    public String addNotification(@ModelAttribute NoticeDTO noticeDTO,
                                  @RequestParam("file") MultipartFile file) {
        String loggedInUserId = "testUser";
        noticeDTO.setMemberId(loggedInUserId);

        log.info("공지사항 추가 요청 데이터: {}", noticeDTO);

        if (noticeDTO.getIsNotice() == null) {
            noticeDTO.setIsNotice(false);
        }

        // 게시글 저장 및 boardId 가져오기
        boardService.insertNotification(noticeDTO); // 게시글 저장
        int boardId = noticeDTO.getBoardId(); // 저장된 게시글의 ID 가져오기

        // 파일 업로드 처리
        if (!file.isEmpty()) {
            String uploadPath = new File("src/main/resources/static/upload").getAbsolutePath();
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
                fileDTO.setFileSize(file.getSize());
                fileDTO.setFileOriginName(fileName);

                fileService.saveFile(fileDTO);
            } catch (IOException e) {
                log.error("파일 저장 실패: {}", e.getMessage());
            }
        }
        return "redirect:/board/notificationList";
    }

    @DeleteMapping("/board/deleteNotification")
    public ResponseEntity<String> deleteNotification(@RequestParam("num") int num, HttpSession session) {
        String loggedInUserId = getLoggedInUserId(session);

        try {
            // 권한 확인
            if (!"testUser".equals(loggedInUserId)) {
                log.warn("삭제 권한 없음: 사용자 ID = {}", loggedInUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
            }

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
    }/////////////////////////////////////


    @GetMapping("/board/editNotification")
    public String editNotification(@RequestParam("num") int num, Model model) {
        // 공지사항 정보
        BoardDTO notification = boardService.findNotificationById(num);
        model.addAttribute("notification", notification);

        // 첨부파일 정보 추가
        FileDTO attachedFile = fileService.getFileByBoardId(num);
        model.addAttribute("attachedFile", attachedFile);

        return "notice/notificationEdit"; // 변경된 뷰 경로
    }

    @PostMapping("/board/editNotification")
    public String updateNotification(@ModelAttribute NoticeDTO noticeDTO,
                                     @RequestParam(value = "newFile", required = false) MultipartFile newFile) {
        log.info("수정 요청 데이터: {}", noticeDTO);

        // 공지사항 수정
        boardService.updateNotification(noticeDTO);

        // 새로운 파일 업로드 처리
        if (newFile != null && !newFile.isEmpty()) {
            String uploadPath = new File("src/main/resources/static/upload").getAbsolutePath();
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

                // 기존 파일 삭제 후 새로운 파일 저장
                fileService.saveFile(fileDTO);
            } catch (IOException e) {
                log.error("파일 저장 실패: {}", e.getMessage());
            }
        }

        return "redirect:/board/notificationDetail?num=" + noticeDTO.getBoardId();
    }

    @PostMapping("/board/addComment")
    public String addComment(@ModelAttribute CommentDTO comment, HttpSession session) {
        // 로그인된 사용자 ID 설정
        String memberId = getLoggedInUserId(session);
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
        String loggedInUserId = getLoggedInUserId(session);

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
        String memberId = getLoggedInUserId(session); // 현재 로그인된 사용자 ID 가져오기

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



}

