package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/user/myPage")
    public String findMyPage(@RequestParam("id") String id, Model model){
        log.info("id ==============={}", id);
        if(id == "") return "redirect:/index";

        MemberDTO member = userService.findMember(id);
        model.addAttribute("member", member);
        log.info("MemberDTO====={}", member);
        return "/user/myPage";
    }

    @PostMapping("/user/myPage")
    public String updateMyPage(MemberDTO member){
        log.info("member==={}",member);
        int result = userService.updateMember(member);
        String url = "redirect:/user/myPage?id="+member.getMemberId();
        return url;
    }

}
