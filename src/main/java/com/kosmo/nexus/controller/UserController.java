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

        // 세션에서 loginUser 객체 가져오기
        LoginDTO loginUser = (LoginDTO) ses.getAttribute("loginUser");
        if (loginUser == null) {
            // 로그아웃 코드 추가해야 할지 이야기(종원)
            return "redirect:/";  // 로그인 페이지로 리다이렉트
        }
        String memberId = loginUser.getMemberId();
        log.info("id ==============={}", memberId);
        if (memberId == null) {     // memberId가 없는 경우
            // 로그아웃 코드 추가해야 할지 이야기(종원)
            return "redirect:/";    // 로그인 페이지로 리다이렉트
        }

        MemberDTO member = userService.findMember(memberId);
        model.addAttribute("member", member);
        log.info("MemberDTO====={}", member);
        return "/user/myPage";
    }

    @PostMapping("/myPage")
    public String updateMyPage(MemberDTO member){
        log.info("member==={}",member);
        int result = userService.updateMember(member);
        String url = "redirect:/user/myPage?id="+member.getMemberId();
        return url;
    }

}
