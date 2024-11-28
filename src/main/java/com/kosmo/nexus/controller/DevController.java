package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.CompanyDTO;
import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.service.AdminService;
import com.kosmo.nexus.service.DevService;
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
@RequestMapping("/dev")
public class DevController {

    @Autowired
    private DevService devService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ServletContext servletContext;

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


    @GetMapping("/userPage")
    public String findCompanyUserPage(@RequestParam("id") String memberId, Model model, HttpSession ses){
        Long companyId = adminService.findCompanyIdByMemberId(memberId);
        if (companyId == null){
            String msg = "접근할 수 없는 정보입니다.";
            String loc = "/admin/memberList";
            return message(model, msg, loc);
        }

        MemberDTO member = adminService.findMemberWithCompanyId(memberId, companyId);
        log.info("MemberDTO====={}", member);
        model.addAttribute("member", member);
        model.addAttribute("companyId", companyId);
        log.info("MemberDTO====={}", member);
        return "dev/devUserPage";
    }

    @PostMapping("/userPage")
    public String UpdateCompanyUserPage(HttpSession ses, Model model, MemberDTO member){
        log.info("수정할 정보 : {}", member);
        String memberId = member.getMemberId();
        Long companyId = member.getCompanyId();

        int result = adminService.updateMemberByAdmin(member, companyId);

        log.info("수정된 데이터의 개수======{}", result);
        String url = "redirect:/dev/userPage?id="+memberId;
        return url;
    }


    @GetMapping("/addMember")
    public String addCompanyMemberList(@RequestParam("id") Long companyId, Model model, HttpSession ses){
        model.addAttribute("companyId",companyId);
        return "/dev/devAddUserPage";
    }

    @PostMapping("/search")
    public String searchCompanyMemberList(@RequestParam("simpleSearch") String search, @RequestParam("searchOption") String option,
                                   @RequestParam("companyId") Long companyId,
                                   HttpSession ses, Model model){

        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();
        log.info("search========={}, option========{}",search, option);
        List<MemberDTO> listMember= adminService.searchMemberList(search, option, companyId);
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
        List<String> departmentsInCompany = adminService.findDepartmentByCompanyId(companyId);
        List<String> ranksInCompany = adminService.findRankByCompanyId(companyId);

        model.addAttribute("departmentsInCompany", departmentsInCompany);
        model.addAttribute("ranksInCompany", ranksInCompany);
        model.addAttribute("listMember", listMember);
        model.addAttribute("departments", departments);
        model.addAttribute("ranks", ranks);
        model.addAttribute("simpleSearch", search);
        model.addAttribute("searchOption", option);
        model.addAttribute("sesLoginId", sesLoginId);
        model.addAttribute("companyId",companyId);
        log.info("SearchMemberList====={}", listMember);
        return "/dev/devMemberList";
    }


    @PostMapping("/adSearch")
    public String adSearchCompanyMemberList(@RequestParam("birthStart") String birthStart,
                                     @RequestParam("birthEnd") String birthEnd,
                                     @RequestParam("hireStart") String hireStart,
                                     @RequestParam("hireEnd") String hireEnd,
                                     @RequestParam("companyId") Long companyId,
                                     HttpSession ses, Model model){

        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();

        if (birthStart.isEmpty()) birthStart = null;
        if (birthEnd.isEmpty()) birthEnd = null;
        if (hireStart.isEmpty()) hireStart = null;
        if (hireEnd.isEmpty())hireEnd = null;

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
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
        List<String> departmentsInCompany = adminService.findDepartmentByCompanyId(companyId);
        List<String> ranksInCompany = adminService.findRankByCompanyId(companyId);

        model.addAttribute("departmentsInCompany", departmentsInCompany);
        model.addAttribute("ranksInCompany", ranksInCompany);
        model.addAttribute("listMember", listMember);
        model.addAttribute("departments", departments);
        model.addAttribute("ranks", ranks);
        model.addAttribute("companyId", companyId);
        model.addAttribute("birthStart", birthStart);
        model.addAttribute("birthEnd", birthEnd);
        model.addAttribute("hireStart", hireStart);
        model.addAttribute("hireEnd", hireEnd);
        model.addAttribute("sesLoginId", sesLoginId);
        model.addAttribute("companyId",companyId);
        log.info("SearchMemberList====={}", listMember);
        return "/dev/devMemberList";
    }


    @PostMapping("/memberDel")
    public String DeleteCompanyMemberList(@RequestParam List<String> memberIds,
                                   @RequestParam("companyId") Long companyId,
                                   HttpSession ses, Model model) throws IOException {

        String sesLoginId = ((LoginDTO) ses.getAttribute("loginUser")).getMemberId();

        // memberIds안에 자기 자신의 아이디가 있다면 리스트에서 삭제하기
        memberIds.remove(sesLoginId);

        // memberIds ID를 이용하여 memberImgName List 받아오기
        List<String> ImgNames = adminService.findImgNamebyIdList(memberIds, companyId);

        // 삭제 리스트에서 같은 회사 id일때만 삭제
        int result = adminService.deleteMemberList(memberIds, companyId);

        // DB에서 삭제 후 memberImgName 경로의 파일 삭제하기
        deleteImages(ImgNames);

        log.info("삭제된 테이블 개수 =========={}", result);
        return "redirect:/dev/companyPage?id="+companyId;
    }

    @PostMapping("/moveDep")
    public String moveCompanyDepartment(@RequestParam List<String> memberIds,
                                 @RequestParam String selDepartment,
                                 @RequestParam("companyId") Long companyId,
                                 HttpSession ses, Model model){

        log.info(memberIds.toString());
        log.info(selDepartment);

        int result = adminService.updateMemberDepartment(memberIds, selDepartment, companyId);

        log.info("부서가 변경된 테이블 개수 =========={}", result);
        return "redirect:/dev/companyPage?id="+companyId;
    }

    @PostMapping("/moveRank")
    public String moveCompanyRank(@RequestParam List<String> memberIds,
                           @RequestParam String selRank,
                           @RequestParam("companyId") Long companyId,
                           HttpSession ses, Model model){
        log.info(memberIds.toString());
        log.info(selRank);

        int result = adminService.updateMemberRank(memberIds, selRank, companyId);

        log.info("직급이 변경된 테이블 개수 =========={}", result);
        return "redirect:/dev/companyPage?id="+companyId;
    }

    @PostMapping("/moveStatus")
    public String moveCompanyStatus(@RequestParam List<String> memberIds,
                             @RequestParam String selStatus,
                             @RequestParam("companyId") Long companyId,
                             HttpSession ses, Model model){
        log.info(memberIds.toString());
        log.info(selStatus);

        int result = adminService.updateMemberStatus(memberIds, selStatus, companyId);

        log.info("직급이 변경된 테이블 개수 =========={}", result);
        return "redirect:/dev/companyPage?id="+companyId;
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
