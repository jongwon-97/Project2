package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.service.AdminService;
import com.kosmo.nexus.service.UserService;
import jakarta.servlet.http.HttpSession;
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
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/admin/memberList")
    public String findMemberList(HttpSession ses, Model model){
        Long sesCompanyId = getLoginUserCompanyId(ses, model);

        List<MemberDTO> listMember= adminService.findMemberList(sesCompanyId);

        model.addAttribute("listMember", listMember);
        log.info("MemberDTOList====={}", listMember);

        return "/admin/memberList";
    }

    @PostMapping("/admin/memberDel")
    public String DeleteMemberList(@RequestParam List<String> membersDel, Model model){
        // 삭제 리스트에 같은 회사가 아닌 사람의 정보가 담긴 경우
        // membersDel.size();


        int result = adminService.deleteMemberList(membersDel);
        log.info("삭제된 테이블 개수 =========={}", result);
        return "redirect:/admin/memberList";
    }

    @GetMapping("/admin/userPage")
    public String findUserPage(@RequestParam("id") String memberId, Model model, HttpSession ses){
        Long memberCompanyId = adminService.findCompanyIdByMemberId(memberId);
        Long sesCompanyId = getLoginUserCompanyId(ses, model);
        if (memberCompanyId == null || sesCompanyId == null || !memberCompanyId.equals(sesCompanyId)){
            String msg = "접근할 수 없는 정보입니다.";
            String loc = "/admin/memberList";
            return message(model, msg, loc);
        }

        MemberDTO member = adminService.findMemberWithCompanyId(memberId, sesCompanyId);
        log.info("MemberDTO====={}", member);
        model.addAttribute("member", member);
        log.info("MemberDTO====={}", member);
        return "admin/userPage";
    }

//    @PostMapping("/admin/userPage")
//    public String UpdateUserPage(@)
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