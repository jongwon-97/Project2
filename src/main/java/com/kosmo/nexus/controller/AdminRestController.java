package com.kosmo.nexus.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.dto.SignupDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class AdminRestController {

    @PostMapping("/admin/addMember")
    public String addUser(@RequestParam("membersData") String membersDataJson, HttpSession ses, Model model) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<SignupDTO> membersData = objectMapper.readValue(membersDataJson, new TypeReference<List<SignupDTO>>() {});

            Long sesCompanyId = getLoginUserCompanyId(ses, model);
            for (SignupDTO member : membersData) {
                member.setMemberRole("User");
                member.setMemberSns(0);
                member.setMemberStatus("활동");
                member.setCompanyId(sesCompanyId);

                // memberPhone이 null이 아니고 길이가 4 이상일 경우, 마지막 4글자를 비밀번호로 설정
                String memberPhone = member.getMemberPhone();  // 전화번호 가져오기
                if (memberPhone != null && memberPhone.length() >= 4) {
                    // 전화번호의 마지막 4글자를 비밀번호로 설정
                    String memberPw = memberPhone.substring(memberPhone.length() - 4);
                    member.setMemberPw(memberPw);  // 비밀번호 설정
                } else {
                    // 전화번호가 유효하지 않은 경우 기본값 설정 (선택 사항)
                    member.setMemberPw("0000");  // 기본값 "0000"으로 설정
                }
            }
            log.info("List<SignupDTO>======{}",membersData);
            // 정규성 확인식 필요

            // signupDTO를 가져와서
            // memberDTO + CompanyDTO(Admin과 같은 companyId를 가진 company table 행을 가져올 것!)

            // Service->mapper를 통한 DB 업데이트 필요



            // json 타입은 "(큰 따옴표)로 문자열을 표현함. Java도 문자열을 "(큰따옴표)로 나타내기 때문에, 아래와 같은 형식으로 작성해야함
            return "{\"status\":\"success\", \"message\":\"회원 등록이 완료되었습니다.\", \"loc\":\"/admin/memberList\"}";

        }catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public Long getLoginUserCompanyId(HttpSession ses, Model model){
        // 세션에서 loginUser 객체 가져오기
        LoginDTO loginUser = (LoginDTO) ses.getAttribute("loginUser");
        if (loginUser == null) {
            message(model, "정상적인 로그인 정보가 아닙니다.", "/logout");
            return null;
        }
        Long companyId = loginUser.getCompanyId();
        if (companyId == null) {     // memberId가 없는 경우
            message(model, "정상적인 로그인 정보가 아닙니다.", "/logout");
            return null;
        }
        return companyId;
    }

    public String message(Model model, String msg, String loc){
        model.addAttribute("msg", msg);
        model.addAttribute("loc", loc);
        return "message";
    }

}
