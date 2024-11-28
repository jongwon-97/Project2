package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.FindAccountDTO;
import com.kosmo.nexus.service.FindAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequiredArgsConstructor
public class FindAccountController {

    private final FindAccountService findAccountService;

    @GetMapping("/findAccount")
    public String findAccountForm(){

        return "findaccount/findhome";
    }

    @GetMapping("/findPAccount")
    public String findPAccountForm() {

        return "findaccount/findPhome";
    }
    @PostMapping("/processPAccount")
    public String processPAccount(
            @RequestParam(required = false) String memberNameEmail,
            @RequestParam(required = false) String memberNamePhone,
            @RequestParam(required = false) String memberEmail,
            @RequestParam(required = false) String memberPhone,
            Model model) {

        // 입력값 처리
        String memberName = (memberNameEmail != null && !memberNameEmail.isEmpty())
                ? memberNameEmail
                : memberNamePhone;
        FindAccountDTO result = null;

        // 분기 처리
        if (memberEmail != null && !memberEmail.isEmpty()) {
            result = findAccountService.findPersonalAccountByEmail(memberName, memberEmail);
        } else if (memberPhone != null && !memberPhone.isEmpty()) {
            result = findAccountService.findPersonalAccountByPhone(memberName, memberPhone);
        }

        if (result == null) {
            log.warn("No result found for memberName={}, memberEmail={}, memberPhone={}",
                    memberName, memberEmail, memberPhone);
            model.addAttribute("result", null);
        } else {
            log.info("Found account: memberId={}", result.getMemberId());
            model.addAttribute("result", result);
        }

        return "findaccount/result";
    }


    @GetMapping("/findBAccount")
    public String findBAccountForm() {

        return "findaccount/findBhome";
    }

    @PostMapping("/processBAccount")
    public String processBAccount(@RequestParam(required = false) String companyNum,
                                  @RequestParam(required = false) String memberNum,
                                  @RequestParam(required = false) String memberNameEmail,
                                  @RequestParam(required = false) String memberEmail,
                                  @RequestParam(required = false) String memberNamePhone,
                                  @RequestParam(required = false) String memberPhone,
                                  Model model) {

        // 입력값 처리: 이름 우선 처리
        String memberName = (memberNameEmail != null && !memberNameEmail.isEmpty())
                ? memberNameEmail
                : memberNamePhone;

        FindAccountDTO result = null;

        // 분기 처리
        if (companyNum != null && !companyNum.isEmpty() && memberNum != null && !memberNum.isEmpty()) {
            // 사업자등록번호 + 사원번호로 찾기
            result = findAccountService.findBusinessAccountByCompanyNum(companyNum, memberNum);
        } else if (memberEmail != null && !memberEmail.isEmpty()) {
            // 이름 + 이메일로 찾기
            result = findAccountService.findBusinessAccountByEmail(memberName, memberEmail);
        } else if (memberPhone != null && !memberPhone.isEmpty()) {
            // 이름 + 전화번호로 찾기
            result = findAccountService.findBusinessAccountByPhone(memberName, memberPhone);
        }

        // 결과 처리
        if (result == null) {
            log.warn("No result found for companyNum={}, memberNum={}, memberName={}, memberEmail={}, memberPhone={}",
                    companyNum, memberNum, memberName, memberEmail, memberPhone);
            model.addAttribute("result", null);
        } else {
            log.info("Found account: memberId={}", result.getMemberId());
            model.addAttribute("result", result);
        }

        return "findaccount/result";
    }
    @GetMapping("/findPassword")
    public String findPassword(){
        return"findaccount/findPassword";
    }

}
