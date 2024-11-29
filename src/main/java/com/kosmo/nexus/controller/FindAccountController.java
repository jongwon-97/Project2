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

        return "findaccount/findAccount";
    }

    @PostMapping("/processAccount")
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
            result = findAccountService.findAccountByCompanyNum(companyNum, memberNum);
        } else if (memberEmail != null && !memberEmail.isEmpty()) {
            // 이름 + 이메일로 찾기
            result = findAccountService.findAccountByEmail(memberName, memberEmail);
        } else if (memberPhone != null && !memberPhone.isEmpty()) {
            // 이름 + 전화번호로 찾기
            result = findAccountService.findAccountByPhone(memberName, memberPhone);
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

    //비밀번호 재설정
    @GetMapping("/findPassword")
    public String findPassword(){
        return"findaccount/findPassword";
    }

    @PostMapping("/processResetPassword")
    public String processResetPassword(
            @RequestParam(required = false) String userIdCompany,
            @RequestParam(required = false) String companyNum,
            @RequestParam(required = false) String memberNum,
            @RequestParam(required = false) String userIdEmail,
            @RequestParam(required = false) String memberNameEmail,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String userIdPhone,
            @RequestParam(required = false) String memberNamePhone,
            @RequestParam(required = false) String userPhone,
            Model model) {

        FindAccountDTO result = null;

        // 분기 처리: 입력 조합에 따라 검색 수행
        if (userIdCompany != null && !userIdCompany.isEmpty()
                && companyNum != null && !companyNum.isEmpty()
                && memberNum != null && !memberNum.isEmpty()) {
            // 아이디 + 사업자등록번호 + 사원번호로 찾기
            result = findAccountService.findPasswordByCompanyNum(userIdCompany, companyNum, memberNum);

        } else if (userIdEmail != null && !userIdEmail.isEmpty()
                && memberNameEmail != null && !memberNameEmail.isEmpty()
                && userEmail != null && !userEmail.isEmpty()) {
            // 아이디 + 이름 + 이메일로 찾기
            result = findAccountService.findPasswordByEmail(userIdEmail, memberNameEmail, userEmail);

        } else if (userIdPhone != null && !userIdPhone.isEmpty()
                && memberNamePhone != null && !memberNamePhone.isEmpty()
                && userPhone != null && !userPhone.isEmpty()) {
            // 아이디 + 이름 + 전화번호로 찾기
            result = findAccountService.findPasswordByPhone(userIdPhone, memberNamePhone, userPhone);
        }

        // 결과 처리
        if (result == null) {
            log.warn("No result found for the provided input values.");
            model.addAttribute("errorMessage", "일치하는 계정을 찾을 수 없습니다. 입력 정보를 확인해주세요.");
            return "findaccount/findPassword"; // 다시 입력 페이지로 이동
        }

        // 성공적으로 계정 찾음
        log.info("Found account: memberId={}", result.getMemberId());
        model.addAttribute("memberId", result.getMemberId());

        // 비밀번호 재설정 페이지로 이동
        return "findaccount/passwordReset";
    }


    @PostMapping("/processNewPassword")
    public String processNewPassword(@RequestParam String memberId,
                                     @RequestParam String newPassword,
                                     Model model) {

        // 비밀번호 해싱 후 업데이트
        try {
            findAccountService.updatePassword(memberId, newPassword);
            model.addAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/"; // 홈 화면으로 리다이렉트
        } catch (Exception e) {
            log.error("Error updating password for memberId={}: {}", memberId, e.getMessage());
            model.addAttribute("errorMessage", "비밀번호 변경 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "findaccount/passwordReset"; // 다시 비밀번호 재설정 페이지로 이동
        }
    }


}
