package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.NoticeDTO;
import com.kosmo.nexus.service.BoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
public class BoardController {

    @Autowired
    private BoardService boardService;

    // 공지사항 목록 페이지 반환
    @GetMapping("/board/notificationList")
    public String notificationList(Model model) {
        List<BoardDTO> notifications = boardService.selectNotificationList();
        model.addAttribute("notifications", notifications);
        log.info("공지사항 목록 출력 결과 == {}", notifications);
        return "notice/notificationList"; // 변경된 뷰 경로
    }

    // 공지사항 상세보기
    @GetMapping("/board/notificationDetail")
    public String notificationDetail(@RequestParam("num") int num, Model model) {
        boardService.increaseViewCount(num);
        BoardDTO notification = boardService.findNotificationById(num);
        model.addAttribute("notification", notification);
        log.info("공지사항 상세보기 데이터 == {}", notification);
        return "notice/notificationDetail"; // 변경된 뷰 경로
    }

    // 공지사항 작성 폼 반환
    @GetMapping("/board/notification")
    public String notificationForm() {
        return "notice/notification"; // 변경된 뷰 경로
    }

    // 공지사항 작성 처리
    @PostMapping("/board/notification")
    public String addNotification(@ModelAttribute NoticeDTO noticeDTO) {
        String loggedInUserId = "testUser"; // 로그인 사용자 ID 설정
        noticeDTO.setMemberId(loggedInUserId);

        log.info("공지사항 추가 요청 데이터: {}", noticeDTO);
        boardService.insertNotification(noticeDTO);
        if (noticeDTO.getIsNotice() == null) {
            noticeDTO.setIsNotice(false);
        }
        boardService.insertNotification(noticeDTO);
        return "redirect:/board/notificationList";
    }

    @DeleteMapping("/board/deleteNotification")
    public ResponseEntity<String> deleteNotification(@RequestParam("num") int num) {
        try {
            boardService.deleteNotification(num);
            return ResponseEntity.ok("게시물이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("게시물 삭제 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시물 삭제에 실패했습니다.");
        }
    }

    @GetMapping("/board/editNotification")
    public String editNotification(@RequestParam("num") int num, Model model) {
        BoardDTO notification = boardService.findNotificationById(num);
        model.addAttribute("notification", notification);
        return "notice/notificationEdit"; // 변경된 뷰 경로
    }

    @PostMapping("/board/editNotification")
    public String updateNotification(@ModelAttribute NoticeDTO noticeDTO) {
        log.info("수정 요청 데이터: {}", noticeDTO);
        boardService.updateNotification(noticeDTO);
        return "redirect:/board/notificationList";
    }
}
