package com.kosmo.nexus.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class FindAccountController {

    @GetMapping("/findAccount")
    public String findAccountForm(){

        return "findaccount/findhome";
    }

    @GetMapping("/findPAccount")
    public String findPAccountForm() {

        return "findaccount/findPhome";
    }
    @PostMapping("/processPAccount")
    public String processPAccount(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String email,
                                  @RequestParam(required = false) String namePhone,
                                  @RequestParam(required = false) String phone,
                                  Model model) {

        // 이름+이메일 처리
        if (email != null) {
            model.addAttribute("result", "이름: " + name + ", 이메일: " + email + "로 찾기");
            // 실제 로직: 서비스 호출 등을 통해 DB에서 계정 검색
        }

        // 이름+휴대전화번호 처리
        if (phone != null) {
            model.addAttribute("result", "이름: " + namePhone + ", 전화번호: " + phone + "로 찾기");
            // 실제 로직: 서비스 호출 등을 통해 DB에서 계정 검색
        }

        // 결과 페이지로 이동
        return "findaccount/result";
    }

    @GetMapping("/findBAccount")
    public String findBAccountForm() {

        return "findaccount/findBhome";
    }


}
