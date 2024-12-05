package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // /user/myPage
    @GetMapping("/myPage")
    public String findMyPage(HttpSession ses, Model model){
//        session에 담겨 있는 객체를 확인하는 코드
//        Enumeration<String> attributeNames = session.getAttributeNames();
//        while (attributeNames.hasMoreElements()) {
//            String attributeName = attributeNames.nextElement();
//            log.info("세션 속성: {} = {}",attributeName, session.getAttribute(attributeName));
//        }

        // 세션에서 memberId를 가져오는 클래스
        String memberId =  getLoginUserId(ses);

        // DB에서 memberId로 MeberDTO 가져오기
        MemberDTO member = userService.findMember(memberId);

        // model을 통해 html에 전달
        model.addAttribute("member", member);
        // log.info("MemberDTO====={}", member);
        return "/user/myPage";
    }

    @PostMapping("/myPage")
    public String updateMyPage( @ModelAttribute MemberDTO member, HttpSession ses,Model model){
        log.info("입력받은 내용===={}",member);
        String sesId = getLoginUserId(ses);
        if(!member.getMemberId().equals(sesId)){
            // memberDTO의 memberId와 Session의 memberId가 다른 경우(비정상적 접근)
            model.addAttribute("msg", "로그아웃되었습니다.");
            model.addAttribute("loc", "/logout");
            return "message";
        }

        log.info("member==={}",member);
        int result = userService.updateMember(member);
        log.info("수정된 데이터의 개수======{}", result);
        return "redirect:/user/myPage";
    }

    public String getLoginUserId(HttpSession ses){
        // 세션에서 loginUser 객체 가져오기
        LoginDTO loginUser = (LoginDTO) ses.getAttribute("loginUser");
        if (loginUser == null) {
            // 로그아웃 코드 추가해야 할지 이야기(종원)
            return "redirect:/";  // 로그인 페이지로 리다이렉트
        }
        String memberId = loginUser.getMemberId();
        if (memberId == null) {     // memberId가 없는 경우
            // 로그아웃 코드 추가해야 할지 이야기(종원)
            return "redirect:/";    // 로그인 페이지로 리다이렉트
        }

        return memberId;
    }

}
