package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.*;
import com.kosmo.nexus.service.AdminService;
import com.kosmo.nexus.service.BoardService;
import com.kosmo.nexus.service.CommentService;
import com.kosmo.nexus.service.FileService;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Controller
@Slf4j
@RequestMapping("/user")
public class UserBoardController {

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
                                   HttpSession session,
                                   Model model) {
        paging.setFindType(findType);
        paging.setFindKeyword(findKeyword);
        Long companyId = ((LoginDTO) session.getAttribute("loginUser")).getCompanyId();

        int notificationCount = boardService.getTotalNotificationCount(paging);
        int generalCount = boardService.getTotalGeneralCount(paging);
        int totalCount = notificationCount + generalCount;

        paging.setTotalCount(generalCount);
        paging.setOneRecordPage(10);
        paging.init();

        List<BoardDTO> list = boardService.selectNotificationListByCompanyId(paging, companyId);

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("notificationCount", notificationCount);
        model.addAttribute("generalCount", generalCount);
        model.addAttribute("notifications", list);
        model.addAttribute("paging", paging);


        return "/notice/userNotificationList";
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
            String loc = "/user/board/notificationList";
            return message(model, msg, loc);
        }

        model.addAttribute("notification", notification);

        // 여러 첨부파일 데이터 가져오기
        List<FileDTO> attachedFiles = fileService.getFilesByBoardId(num);
        model.addAttribute("attachedFiles", attachedFiles);

        log.info("첨부파일 목록 데이터: {}", attachedFiles);

        // 댓글 데이터 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(num);
        model.addAttribute("commentList", commentList);

        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        model.addAttribute("loginUser", loggedInUserId);

        boardService.increaseViewCount(num);
        return "notice/userNotificationDetail";
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
        return "redirect:/user/board/notificationDetail?num=" + comment.getBoardId();
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
            return "redirect:/user/board/notificationDetail?num=" + boardId;
        } catch (IllegalAccessException e) {
            log.error("댓글 삭제 실패 - 권한 없음: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 권한 없음");
            return "redirect:/user/board/notificationDetail?num=" + boardId;
        } catch (Exception e) {
            log.error("댓글 삭제 실패 - 서버 오류: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 서버 오류");
            return "redirect:/user/board/notificationDetail?num=" + boardId;
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

            return "redirect:/user/board/notificationDetail?num=" + boardId; // 댓글이 속한 공지로 리다이렉트
        } catch (IllegalAccessException e) {
            log.error("댓글 수정 실패: 권한 없음", e);
            model.addAttribute("alertMessage", "수정 실패: 권한이 없습니다.");
            return "redirect:/user/board/notificationDetail?num=" + commentId; // 실패 시에도 같은 페이지로 리다이렉트
        } catch (Exception e) {
            log.error("댓글 수정 실패: 서버 오류", e);
            model.addAttribute("alertMessage", "수정 실패: 서버 오류");
            return "redirect:/user/board/notificationDetail?num=" + commentId;
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
            HttpSession session,
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

        Long companyId = ((LoginDTO) session.getAttribute("loginUser")).getCompanyId();
        // Q&A 게시글 목록 조회
//        List<BoardDTO> qnaList = boardService.selectQnaList(paging);
        List<BoardDTO> qnaList = boardService.selectQnaListByCompanyID(paging, companyId);


        // 모델에 데이터 전달
        model.addAttribute("qnaList", qnaList); // Q&A 게시글 리스트
        model.addAttribute("paging", paging); // 페이징 데이터
        model.addAttribute("findType", findType); // 검색 유형
        model.addAttribute("findKeyword", findKeyword); // 검색 키워드
        log.info("qnaList, paging, findType, findKeyword======{},{},{},{}",qnaList, paging, findType, findKeyword);
        return "qna/userQnaList"; // Q&A 목록 페이지로 이동
    }

    // QnA 상세보기
    @GetMapping("/board/qnaDetail")
    public String qnaDetail(@RequestParam("num") int num, Model model, HttpSession session) {
        // QnA 데이터 가져오기
        BoardDTO qna = boardService.findQnaById(num);

        String boardMemberId = qna.getMemberId();
        Long logCompanyId = ((LoginDTO) session.getAttribute("loginUser")).getCompanyId();
        Long boardCompanyID = adminService.findCompanyIdByMemberId(boardMemberId);
        if (!logCompanyId.equals(boardCompanyID)){
            String msg = "접근 권한이 없는 게시글 입니다.";
            String loc = "/board/qnaList";
            return message(model, msg, loc);
        }

        String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
        if(qna.getDisclosureStatus().equals("비공개")){
            // 로그인된 사용자 ID 가져오기
            if(!qna.getMemberId().equals(loggedInUserId)){
                String msg = "비공개 게시글 입니다.";
                String loc = "/board/qnaList";
                return message(model, msg, loc);
            }
        }
        // 조회수 증가
        boardService.increaseQnaViewCount(num);

        model.addAttribute("qna", qna);
        model.addAttribute("loginUser", loggedInUserId);
        // 댓글 및 대댓글 데이터 가져오기
        List<CommentDTO> commentList = commentService.getCommentsByBoardId(num);
        model.addAttribute("commentList", commentList);

        log.info("QnA 상세보기 데이터 == {}", qna);
        log.info("댓글 목록 데이터 == {}", commentList);

        return "qna/userQnaDetail";
    }

    @GetMapping("/board/qna")
    public String goToQnaForm(){
        return "qna/userQna";
    }

    @PostMapping("/board/qna")
    public String insertQna(@ModelAttribute BoardDTO boardDTO,
                            HttpSession ses){
        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();
        boardDTO.setMemberId(sesLoginId);
        boardService.insertQna(boardDTO);
        return "redirect:/user/board/qnaList";
    }

    // QnA 수정 폼 반환
    @GetMapping("/board/editQna")
    public String editQna(@RequestParam("num") int num, Model model) {
        // Q&A 데이터 가져오기
        BoardDTO qna = boardService.findQnaById(num);
        model.addAttribute("qna", qna);

        return "/qna/userQnaEdit"; // 수정 페이지 경로
    }

    // QnA 수정 처리
    @PostMapping("/board/editQna")
    public String updateQna(@ModelAttribute BoardDTO boardDTO) {
        // Q&A 수정 처리
        boardService.updateQna(boardDTO);

        return "redirect:/user/board/qnaDetail?num=" + boardDTO.getBoardId();
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
        return "redirect:/user/board/qnaDetail?num=" + comment.getBoardId();
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
            return "redirect:/user/board/qnaDetail?num=" + boardId;
        } catch (IllegalAccessException e) {
            log.error("QnA 댓글 삭제 실패 - 권한 없음: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 권한 없음");
            return "redirect:/user/board/qnaDetail?num=" + boardId;
        } catch (Exception e) {
            log.error("QnA 댓글 삭제 실패 - 서버 오류: {}", e.getMessage());
            model.addAttribute("alertMessage", "삭제 실패: 서버 오류");
            return "redirect:/user/board/qnaDetail?num=" + boardId;
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

            return "redirect:/user/board/qnaDetail?num=" + boardId; // 댓글이 속한 QnA로 리다이렉트
        } catch (IllegalAccessException e) {
            log.error("QnA 댓글 수정 실패: 권한 없음", e);
            model.addAttribute("alertMessage", "수정 실패: 권한이 없습니다.");
            return "redirect:/user/board/qnaDetail?num=" + boardId; // 실패 시에도 같은 페이지로 리다이렉트
        } catch (Exception e) {
            log.error("QnA 댓글 수정 실패: 서버 오류", e);
            model.addAttribute("alertMessage", "수정 실패: 서버 오류");
            return "redirect:/user/board/qnaDetail?num=" + boardId;
        }
    }
    @PostMapping("/board/deleteQna")
    public String deleteQna(@RequestParam("boardId") int boardId, HttpSession session, Model model) {
        try {
            // QnA 삭제 처리
            String loggedInUserId = ((LoginDTO) session.getAttribute("loginUser")).getMemberId();
            boardService.deleteQna(boardId);
            log.info("QnA 삭제 성공: {}", boardId);

            return "redirect:/user/board/qnaList"; // 삭제 후 QnA 목록으로 리다이렉트
        } catch (Exception e) {
            log.error("QnA 삭제 실패", e);
            model.addAttribute("alertMessage", "QnA 삭제에 실패했습니다.");
            return "redirect:/user/board/qnaDetail?num=" + boardId; // 실패 시 해당 QnA 상세 페이지로 리다이렉트
        }
    }

    public String message(Model model, String msg, String loc){
        model.addAttribute("msg", msg);
        model.addAttribute("loc", loc);
        return "message";
    }
}
