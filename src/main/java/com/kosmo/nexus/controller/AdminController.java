package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.service.AdminService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ServletContext servletContext;

    @GetMapping("/admin/memberList")
    public String findMemberList(HttpSession ses, Model model){
        Long sesCompanyId = getLoginUserCompanyId(ses, model);

        List<MemberDTO> listMember= adminService.findMemberList(sesCompanyId);

        model.addAttribute("listMember", listMember);
        log.info("MemberDTOList====={}", listMember);

        return "/admin/memberList";
    }

    @GetMapping("/admin/addMember")
    public String addUserPage(){
        return "admin/addUserPage";
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

    @PostMapping("/admin/userPage")
    public String UpdateUserPage(HttpSession ses, Model model, MemberDTO member){
        String memberId = member.getMemberId();
        Long memberCompanyId = adminService.findCompanyIdByMemberId(memberId);
        Long sesCompanyId = getLoginUserCompanyId(ses, model);

        log.info("mid ====={}, memCid====={}, sesCid===={}", memberId, memberCompanyId, sesCompanyId);
        if(!memberCompanyId.equals(sesCompanyId)){
            log.info("cid가 일치하지 않을경우");
            // memberDTO의 CompanyId와 Session의 CompanyId가 다른 경우(비정상적 접근)
            model.addAttribute("msg", "접근 권한이 없습니다.");
            model.addAttribute("loc", "/admin/memberList");
            return "message";
        }
        log.info("cid가 일치하는 경우");
        log.info("member==={}",member);
        int result = adminService.updateMemberByAdmin(member, sesCompanyId);

        log.info("수정된 데이터의 개수======{}", result);
        String url = "redirect:/admin/userPage?id="+memberId;
        return url;
    }


    @PostMapping("/admin/memberDel")
    public String DeleteMemberList(@RequestParam List<String> membersDel, HttpSession ses, Model model) throws IOException {
        Long sesCompanyId = getLoginUserCompanyId(ses, model);

        // membersDel의 ID를 이용하여 memberImgName List 받아오기
        List<String> ImgNames = adminService.findImgNamebyIdList(membersDel, sesCompanyId);

        // 삭제 리스트에서 같은 회사 id일때만 삭제
        int result = adminService.deleteMemberList(membersDel, sesCompanyId);

        // DB에서 삭제 후 memberImgName 경로의 파일 삭제하기
        deleteImages(ImgNames);

        log.info("삭제된 테이블 개수 =========={}", result);
        return "redirect:/admin/memberList";
    }


    @PostMapping("/admin/search")
    public String searchMemberList(@RequestParam("simpleSearch") String search, @RequestParam("searchOption") String option,
                                   HttpSession ses, Model model){
            Long sesCompanyId = getLoginUserCompanyId(ses, model);
            log.info("search========={}, option========{}",search, option);
            List<MemberDTO> listMember= adminService.searchMemberList(search, option, sesCompanyId);

            model.addAttribute("listMember", listMember);
            log.info("SearchMemberList====={}", listMember);
        return "/admin/memberList";
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

    public void deleteImages(List<String> imagePaths) throws IOException {
        // imagePath가 상대 경로로 저장되었으므로, 절대 경로를 만들어야 할 수도 있습니다.
        for (int i = 0; i < imagePaths.size(); i++) {
            String imagePath = imagePaths.get(i);
            Path path = Paths.get(servletContext.getRealPath(imagePath));

            // 파일이 존재하면 삭제
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("이미지 파일 삭제됨: {}", imagePath);
            } else {
                log.warn("이미지 파일을 찾을 수 없음: {}", imagePath);
            }
        }
    }

    public String message(Model model, String msg, String loc){
        model.addAttribute("msg", msg);
        model.addAttribute("loc", loc);
        return "message";
    }



}