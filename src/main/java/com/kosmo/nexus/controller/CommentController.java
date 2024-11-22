package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.CommentDTO;
import com.kosmo.nexus.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 댓글 작성 폼 및 댓글 목록 출력
    @GetMapping("/writeComment")
    public String showCommentForm(Model model) {
        // 전체 댓글 및 대댓글 목록 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(1L); //게시판 ID 1
        model.addAttribute("commentList", commentList);
        return "comment/commentForm"; // commentForm.html 반환
    }//------------------------------

    // 댓글 작성 후 DB에 저장하는 POST 요청(댓글 작성)
    @PostMapping("/comment/write")
    public String commentWrite(CommentDTO comment, Model model) {
        commentService.saveComment(comment);  // 댓글 또는 대댓글 저장
        return "redirect:/writeComment";  // 댓글 목록 페이지로 리다이렉트
    }//---------------------------------
    //대댓글 작성
    @PostMapping("/comment/reply")
    public String replyWrite(CommentDTO comment, Model model) {
        // 대댓글 DB에 저장
        commentService.saveComment(comment);
        // 해당 게시글에 대한 댓글 및 대댓글을 모두 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(comment.getBoardId());
        model.addAttribute("commentList", commentList);
        return "redirect:/writeComment";   // 대댓글을 추가한 후 댓글 목록 페이지로 리다이렉트
    }//-------------------------------
    //댓글 및 대댓글 삭제
    @PostMapping("/comment/delete")
    public String deleteComment(@RequestParam Long commentId, @RequestParam String memberId, Model model) {
        try {
            commentService.deleteCommentAndReplies(commentId, memberId);
            // 댓글 삭제 후 성공적으로 리다이렉트
            return "redirect:/writeComment";
        } catch (IllegalAccessException e) {
            // 권한 없음
            model.addAttribute("alertMessage", "삭제 실패: 권한 없음");
            return "redirect:/writeComment";  // 리다이렉트 후 페이지에서 alert 표시
        } catch (Exception e) {
            // 서버 오류
            model.addAttribute("alertMessage", "삭제 실패: 서버 오류");
            return "redirect:/writeComment";  // 리다이렉트 후 페이지에서 alert 표시
        }
    }//----------------------------

    // 댓글 및 대댓글 수정
    @PostMapping("/comment/edit")
    public String editComment(@RequestParam Long commentId,
                              @RequestParam String commentContent,
                              @RequestParam String memberId,
                              Model model) {
        try {
            // 수정 요청 처리
            CommentDTO comment = new CommentDTO();
            comment.setCommentId(commentId);
            comment.setCommentContent(commentContent);
            comment.setMemberId(memberId);
            commentService.updateComment(comment, memberId);
            return "redirect:/writeComment"; // 수정 성공 시 목록으로 리다이렉트
        } catch (IllegalAccessException e) {
            model.addAttribute("alertMessage", "수정 실패: 권한이 없습니다.");
            return "redirect:/writeComment";
        }//------------------------------
    }

}
