package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.BoardDTO;
import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.dto.PagingDTO;
import com.kosmo.nexus.dto.SeasonDTO;
import com.kosmo.nexus.exception.NoMemberException;
import com.kosmo.nexus.service.BoardService;
import com.kosmo.nexus.service.EventService;
import com.kosmo.nexus.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class LoginController {

    private final LoginService loginService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private EventService eventService;

    @Autowired
    public LoginController(LoginService loginService) { // 생성자 주입
        this.loginService = loginService;
    }

    //개인회원 로그인 페이지
    @GetMapping("/pLogin")
    public String personalLoginForm(){

        return "member/personal"; // static/member/personal.html 로그인 페이지로 이동
    }

    // 개인회원 로그인 프로세스
    @PostMapping("pLogin")
    @ResponseBody
    public String pLoginProcess(LoginDTO tmpUser, HttpSession ses,
                                @RequestParam(name = "saveId", defaultValue = "false") boolean saveId,
                                HttpServletResponse res) throws IOException {
        try {
            // 응답의 Content-Type을 설정 (HTML 형식으로 반환)
            res.setContentType("text/html;charset=UTF-8");
            // 1. 유효성 체크 (userId, userPw 빈 문자열 여부 확인)
            // 빈 문자열일 경우 다시 로그인 페이지로 리디렉트
            if (tmpUser.getMemberId() == null || tmpUser.getMemberId().trim().isBlank() ||
                    tmpUser.getMemberPw() == null || tmpUser.getMemberPw().trim().isBlank()) {
                return "<script>alert('아이디와 비밀번호를 입력해주세요.'); window.location.href = '/pLogin';</script>";
            }

            // 2. userService의 loginCheck(tmpUser) 호출
            // - 회원일 경우: LoginDTO 반환
            // - 회원이 아닐 경우: NoMemberException 발생
            LoginDTO loginUser = loginService.loginCheck(tmpUser);

            if (loginUser != null) {
                // 로그인 성공: 세션에 사용자 정보 저장
                ses.setAttribute("loginUser", loginUser);
                // 쿠키 설정
                Cookie ck = new Cookie("uid", loginUser.getMemberId());

                if (saveId) {
                    // '아이디 저장' 옵션이 체크된 경우: 쿠키를 7일간 유지
                    ck.setMaxAge(7 * 24 * 60 * 60);
                } else {
                    // 체크되지 않은 경우: 쿠키 삭제
                    ck.setMaxAge(0); // 유효시간을 0으로 설정하면 쿠키가 삭제됨
                }

                ck.setPath("/"); // 쿠키 경로 설정
                // 보안 옵션 (HttpOnly, Secure)은 필요 시 활성화
                // ck.setHttpOnly(true); // 클라이언트 스크립트에서 쿠키 접근 금지
                // ck.setSecure(true);  // HTTPS에서만 쿠키 전송
                res.addCookie(ck);

                // 부모 창(home)으로 이동하는 스크립트 반환
                return "<script>window.parent.location.href = '/user/home';</script>";
            }

            // 이 코드에 도달하지 않지만 안전을 위해 추가
            return "<script>alert('예기치 않은 오류가 발생했습니다.'); window.location.href = '/pLogin';</script>";

        } catch (NoMemberException e) {
            // 3. 로그인 실패 시 처리
            // - 예외 발생 시: 로그인 페이지로 다시 이동
            return "<script>alert('" + e.getMessage() + "'); window.location.href = '/pLogin';</script>";
        }
    }

    @GetMapping("/session-status")
    @ResponseBody
    public ResponseEntity<?> sessionStatus(HttpSession ses) {
        LoginDTO loginUser = (LoginDTO) ses.getAttribute("loginUser");
        if (loginUser != null) {
            return ResponseEntity.ok(Map.of(
                    "memberName", loginUser.getMemberName(),
                    "memberRole", loginUser.getMemberRole()
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인되지 않았습니다."));
    }

    //기업회원 로그인 페이지
    @GetMapping("/bLogin")
    public String businessLoginForm() {
        return "member/business"; // static/member/business.html
    }

    //기업회원 로그인 프로세스
    @PostMapping("/bLogin")
    @ResponseBody
    public String bLoginProcess(LoginDTO tmpUser, HttpSession ses,
                                @RequestParam(name = "saveId", defaultValue = "false") boolean saveId,
                                HttpServletResponse res) throws IOException {
        try {
            // 응답의 Content-Type을 설정 (HTML 형식으로 반환)
            res.setContentType("text/html;charset=UTF-8");
            // 1. 유효성 체크 (userId, userPw 빈 문자열 여부 확인)
            // 빈 문자열일 경우 다시 로그인 페이지로 리디렉트
            if (tmpUser.getMemberId() == null || tmpUser.getMemberId().trim().isBlank() ||
                    tmpUser.getMemberPw() == null || tmpUser.getMemberPw().trim().isBlank()) {
                return "<script>alert('아이디와 비밀번호를 입력해주세요.'); window.location.href = '/bLogin';</script>";
            }

            // 2. 로그인 로직 처리
            LoginDTO loginUser = loginService.businessLoginCheck(tmpUser);

            // 3. 로그인 성공: 세션에 사용자 정보 저장
            ses.setAttribute("loginUser", loginUser);
            // 4. 쿠키 설정
            Cookie ck = new Cookie("uid", loginUser.getMemberId());
            ck.setMaxAge(saveId ? 7 * 24 * 60 * 60 : 0); // 7일 유지 또는 즉시 삭제
            ck.setPath("/");
            res.addCookie(ck);

            // 5. 권한별 리다이렉트
            String redirectUrl = switch (loginUser.getMemberRole().toLowerCase()) {
                case "user" -> "/user/home";   // 일반 사용자
                case "admin" -> "/admin/home"; // 관리자
                case "dev" -> "/dev/home";    // 개발자
                default -> "/bLogin";          // 기타 예외 상황
            };
            return "<script>window.parent.location.href = '" + redirectUrl + "';</script>";

        } catch (NoMemberException e) {
            // 6. 로그인 실패 처리
            return "<script>alert('" + e.getMessage() + "'); window.location.href = '/bLogin';</script>";
        }
    }
    // Admin 홈
    @GetMapping("/admin/home")
    public String adminHome(HttpSession session, Model model) {
        return validateRole(session, "Admin", "member/adminhome", model); // 역할 확인 후 이동
    }

    // User 홈
    @GetMapping("/user/home")
    public String userHome(HttpSession session, Model model) {
        return validateRole(session, "User", "member/userhome", model);
    }

    // Dev 홈
    @GetMapping("/dev/home")
    public String devHome(HttpSession session, Model model) {
        return validateRole(session, "Dev", "member/devhome", model);
    }

    // 권한 확인 메서드 (공통화)
    private String validateRole(HttpSession session, String requiredRole, String successPage
                                , Model model) {
        LoginDTO loginUser = (LoginDTO) session.getAttribute("loginUser");
        if (loginUser == null || !requiredRole.equalsIgnoreCase(loginUser.getMemberRole())) {
            return "redirect:/accessDenied";
        }
        model.addAttribute("member", loginUser);
        Long companyId = ((LoginDTO) session.getAttribute("loginUser")).getCompanyId();
        PagingDTO paging = new PagingDTO();
        paging.setTotalCount(5);
        paging.setOneRecordPage(5);
        paging.init();

        if(successPage.equals("member/adminhome")){
            int qna = boardService.selectUnansByCompanyID(companyId);
            model.addAttribute("qna", qna);

        } else if (successPage.equals("member/devhome")){
            int qna = boardService.selectUnans();
            model.addAttribute("qna", qna);
        }
        List<SeasonDTO> seasonList = eventService.searchSeasons("", "모집중");
        if (seasonList.size() > 5) {
            seasonList = seasonList.subList(0, 5); // 0부터 5번째 전까지의 항목만 반환
        }
        model.addAttribute("seasonList", seasonList);

        List<BoardDTO> list = boardService.selectNotificationListByCompanyId(paging, companyId);
        model.addAttribute("notifications", list);
        return successPage;
    }

    // 권한 없음 페이지
    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "error/accessDenied";
    }


    @PostMapping("/logout")
    public String logout(HttpSession ses) {
        ses.invalidate(); // 세션 무효화
        return "redirect:/";
    }
}