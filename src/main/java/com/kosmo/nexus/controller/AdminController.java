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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ServletContext servletContext;

    @GetMapping("/memberList")
    public String findMemberList(HttpSession ses, Model model){
        Long sesCompanyId = getLoginUserCompanyId(ses, model);
        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();
        List<String> departmentsInCompany = adminService.findDepartmentByCompanyId(sesCompanyId);
        List<String> ranksInCompany = adminService.findRankByCompanyId(sesCompanyId);
        List<MemberDTO> listMember= adminService.findMemberList(sesCompanyId);
        List<String> departments = listMember.stream()
                .map(MemberDTO::getMemberDepartment) // 단일 값을 추출
                .distinct() // 중복 제거
                .sorted() // 오름차순 정렬
                .collect(Collectors.toList()); // List<String>으로 수집
        List<String> ranks = listMember.stream()
                .map(MemberDTO::getMemberRank) // 단일 값을 추출
                .distinct() // 중복 제거
                .sorted() // 오름차순 정렬
                .collect(Collectors.toList()); // List<String>으로 수집

        model.addAttribute("listMember", listMember);
        model.addAttribute("departments", departments);
        model.addAttribute("ranks", ranks);
        model.addAttribute("departmentsInCompany", departmentsInCompany);
        model.addAttribute("ranksInCompany", ranksInCompany);
        model.addAttribute("sesLoginId", sesLoginId);


        log.info("MemberDTOList====={}", listMember);
        log.info("DepartmentList====={}", departments);
        log.info("RankList====={}", ranks);

        return "/admin/memberList";
    }

    @GetMapping("/addMember")
    public String addUserPage(){
        return "admin/addUserPage";
    }


    @GetMapping("/userPage")
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

    @PostMapping("/userPage")
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
        // log.info("cid가 일치하는 경우");
        // log.info("member==={}",member);

        int result = adminService.updateMemberByAdmin(member, sesCompanyId);

        log.info("수정된 데이터의 개수======{}", result);
        String url = "redirect:/admin/userPage?id="+memberId;
        return url;
    }


    @PostMapping("/memberDel")
    public String DeleteMemberList(@RequestParam List<String> memberIds, HttpSession ses, Model model) throws IOException {
        Long sesCompanyId = getLoginUserCompanyId(ses, model);
        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();

        // memberIds안에 자기 자신의 아이디가 있다면 리스트에서 삭제하기
        memberIds.remove(sesLoginId);

        // memberIds ID를 이용하여 memberImgName List 받아오기
        List<String> ImgNames = adminService.findImgNamebyIdList(memberIds, sesCompanyId);

        // 삭제 리스트에서 같은 회사 id일때만 삭제
        int result = adminService.deleteMemberList(memberIds, sesCompanyId);

        // DB에서 삭제 후 memberImgName 경로의 파일 삭제하기
        deleteImages(ImgNames);

        log.info("삭제된 테이블 개수 =========={}", result);
        return "redirect:/admin/memberList";
    }


    @PostMapping("/search")
    public String searchMemberList(@RequestParam("simpleSearch") String search, @RequestParam("searchOption") String option,
                                   HttpSession ses, Model model){
        Long sesCompanyId = getLoginUserCompanyId(ses, model);
        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();
        log.info("search========={}, option========{}",search, option);
        List<MemberDTO> listMember= adminService.searchMemberList(search, option, sesCompanyId);
        List<String> departments = listMember.stream()
                .map(MemberDTO::getMemberDepartment) // 단일 값을 추출
                .distinct() // 중복 제거
                .sorted() // 오름차순 정렬
                .collect(Collectors.toList()); // List<String>으로 수집
        List<String> ranks = listMember.stream()
                .map(MemberDTO::getMemberRank) // 단일 값을 추출
                .distinct() // 중복 제거
                .sorted() // 오름차순 정렬
                .collect(Collectors.toList()); // List<String>으로 수집
        List<String> departmentsInCompany = adminService.findDepartmentByCompanyId(sesCompanyId);
        List<String> ranksInCompany = adminService.findRankByCompanyId(sesCompanyId);

        model.addAttribute("departmentsInCompany", departmentsInCompany);
        model.addAttribute("ranksInCompany", ranksInCompany);
        model.addAttribute("listMember", listMember);
        model.addAttribute("departments", departments);
        model.addAttribute("ranks", ranks);
        model.addAttribute("simpleSearch", search);
        model.addAttribute("searchOption", option);
        model.addAttribute("sesLoginId", sesLoginId);
        log.info("SearchMemberList====={}", listMember);
        return "/admin/memberList";
    }

    @PostMapping("/adSearch")
    public String adSearchMemberList(@RequestParam("birthStart") String birthStart,
                                     @RequestParam("birthEnd") String birthEnd,
                                     @RequestParam("hireStart") String hireStart,
                                     @RequestParam("hireEnd") String hireEnd,
                                     HttpSession ses, Model model){
        Long sesCompanyId = getLoginUserCompanyId(ses, model);
        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();

        if (birthStart.isEmpty()) birthStart = null;
        if (birthEnd.isEmpty()) birthEnd = null;
        if (hireStart.isEmpty()) hireStart = null;
        if (hireEnd.isEmpty())hireEnd = null;

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", sesCompanyId);
        params.put("birthStart", birthStart);
        params.put("birthEnd", birthEnd);
        params.put("hireStart", hireStart);
        params.put("hireEnd", hireEnd);
        List<MemberDTO> listMember = adminService.searchMemberListByDate(params);
        // List<MemberDTO>에서 memberDepartment 추출하여 List<String>으로 변환
        List<String> departments = listMember.stream()
                .map(MemberDTO::getMemberDepartment) // 단일 값을 추출
                .distinct() // 중복 제거
                .sorted() // 오름차순 정렬
                .collect(Collectors.toList()); // List<String>으로 수집
        List<String> ranks = listMember.stream()
                .map(MemberDTO::getMemberRank) // 단일 값을 추출
                .distinct() // 중복 제거
                .sorted() // 오름차순 정렬
                .collect(Collectors.toList()); // List<String>으로 수집
        List<String> departmentsInCompany = adminService.findDepartmentByCompanyId(sesCompanyId);
        List<String> ranksInCompany = adminService.findRankByCompanyId(sesCompanyId);

        model.addAttribute("departmentsInCompany", departmentsInCompany);
        model.addAttribute("ranksInCompany", ranksInCompany);
        model.addAttribute("listMember", listMember);
        model.addAttribute("departments", departments);
        model.addAttribute("ranks", ranks);
        model.addAttribute("companyId", sesCompanyId);
        model.addAttribute("birthStart", birthStart);
        model.addAttribute("birthEnd", birthEnd);
        model.addAttribute("hireStart", hireStart);
        model.addAttribute("hireEnd", hireEnd);
        model.addAttribute("sesLoginId", sesLoginId);
        log.info("SearchMemberList====={}", listMember);
        return "/admin/memberList";
    }

    @PostMapping("/moveDep")
    public String moveDepartment(@RequestParam List<String> memberIds,
                                 @RequestParam String selDepartment,
                                 HttpSession ses, Model model){
        Long sesCompanyId = getLoginUserCompanyId(ses, model);
        log.info(memberIds.toString());
        log.info(selDepartment);

        int result = adminService.updateMemberDepartment(memberIds, selDepartment, sesCompanyId);

        log.info("부서가 변경된 테이블 개수 =========={}", result);
        return "redirect:/admin/memberList";
    }

    @PostMapping("/moveRank")
    public String moveRank(@RequestParam List<String> memberIds,
                                 @RequestParam String selRank,
                                 HttpSession ses, Model model){
        Long sesCompanyId = getLoginUserCompanyId(ses, model);
        log.info(memberIds.toString());
        log.info(selRank);

        int result = adminService.updateMemberRank(memberIds, selRank, sesCompanyId);

        log.info("직급이 변경된 테이블 개수 =========={}", result);
        return "redirect:/admin/memberList";
    }

    @PostMapping("/moveStatus")
    public String moveStatus(@RequestParam List<String> memberIds,
                           @RequestParam String selStatus,
                           HttpSession ses, Model model){
        Long sesCompanyId = getLoginUserCompanyId(ses, model);
        log.info(memberIds.toString());
        log.info(selStatus);

        int result = adminService.updateMemberStatus(memberIds, selStatus, sesCompanyId);

        log.info("직급이 변경된 테이블 개수 =========={}", result);
        return "redirect:/admin/memberList";
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