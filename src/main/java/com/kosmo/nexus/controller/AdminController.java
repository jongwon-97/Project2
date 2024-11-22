package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.service.AdminService;
import com.kosmo.nexus.service.UserService;
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
    public String findMemberList(Model model){
        int companyId = 1;
        List<MemberDTO> listMember= adminService.findMemberList(companyId);

        model.addAttribute("listMember", listMember);
        log.info("MemberDTOList====={}", listMember);

        return "/admin/memberList";
    }

    @PostMapping("/admin/memberDel")
    public String DeleteMemberList(@RequestParam List<String> membersDel, Model model){
        int result = adminService.deleteMemberList(membersDel);
        log.info("삭제된 테이블 개수 =========={}", result);
        return "redirect:/admin/memberList";
    }

    @GetMapping("/admin/userPage")
    public String findUserPage(@RequestParam("id") String id, Model model){
        log.info("admin ID ======={}", id);
        MemberDTO member = userService.findMember(id);
        model.addAttribute("member", member);
        log.info("MemberDTO====={}", member);
        return "admin/userPage";
    }

}
