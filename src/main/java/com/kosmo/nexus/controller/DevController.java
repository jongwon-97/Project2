package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.CompanyDTO;
import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.service.AdminService;
import com.kosmo.nexus.service.DevService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/dev")
public class DevController {

    @Autowired
    private DevService devService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/companyList")
    public String findCompanyList(HttpSession ses, Model model){
        String sesRole = getLoginUserRole(ses, model);
        List<CompanyDTO> companyList = devService.findCompanyList();
        model.addAttribute("companyList", companyList);
        return "/dev/companyList";
    }

    @GetMapping("/companyPage")
    public String findCompanyMemberList(@RequestParam("id") Long companyId, Model model, HttpSession ses){
        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();
        List<String> departmentsInCompany = adminService.findDepartmentByCompanyId(companyId);
        List<String> ranksInCompany = adminService.findRankByCompanyId(companyId);
        List<MemberDTO> listMember= adminService.findMemberList(companyId);
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
        model.addAttribute("companyId",companyId);


        log.info("MemberDTOList====={}", listMember);
        log.info("DepartmentList====={}", departments);
        log.info("RankList====={}", ranks);
        return "/dev/devMemberList";
    }


    public String getLoginUserRole(HttpSession ses, Model model){
        // 세션에서 loginUser 객체 가져오기
        LoginDTO loginUser = (LoginDTO) ses.getAttribute("loginUser");
        if (loginUser == null) {
            return message(model, "정상적인 로그인 정보가 아닙니다.", "/logout");
        }
        String sesRole = loginUser.getMemberRole();
        if (!sesRole.equals("Dev")) {     // memberId가 없는 경우
            return message(model, "관리자만 접근할 수 있습니다.", "/logout");
        }
        return sesRole;
    }

    public String message(Model model, String msg, String loc){
        model.addAttribute("msg", msg);
        model.addAttribute("loc", loc);
        return "message";
    }
}
