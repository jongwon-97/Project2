package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.CompanyDTO;
import com.kosmo.nexus.dto.SignupDTO;
import com.kosmo.nexus.service.SignupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@Slf4j
public class SignupController {
    @Autowired
    private SignupService signupService;

    @GetMapping("/pSignup")
    public String pSignupForm(){

        return "member/pSignup";
    }

    @PostMapping("/pSignup")
    public String pSignup(SignupDTO signup,
                          @RequestParam("memberImg") MultipartFile memberImg,
                          HttpServletRequest req,
                          Model model){
        //log.info("signup====={}", signup);

        // 업로드 디렉토리 경로 설정
        String upDir = req.getServletContext().getRealPath("/member_img");
        File dir = new File(upDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 디렉토리 생성
        }

        // 파일 처리
        if (!memberImg.isEmpty() && memberImg.getOriginalFilename() != null) {
            try {
                String originalFileName = memberImg.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String newFileName = uuid + "_" + originalFileName.replaceAll("\\s+", "_");
                memberImg.transferTo(new File(upDir, newFileName)); // 저장
                signup.setMemberImgName(newFileName); // DTO에 파일 이름 설정
            } catch (IOException e) {
                //log.error("파일 업로드 실패: {}", e.getMessage());
                model.addAttribute("msg", "파일 업로드 중 오류가 발생했습니다.");
                model.addAttribute("loc", "/pSignup");
                return "message";
            }
        } else {
            signup.setMemberImgName("images/noIdp.png"); // 기본 이미지 설정
        }

        boolean isValid = signupService.validateSignup(signup);
        if (!isValid) {
            model.addAttribute("msg", "입력한 정보에 오류가 있습니다. 다시 확인해주세요.");
            model.addAttribute("loc", "/pSignup");
            return "message";
        }
        //서비스 객체의 메서드를 호출
        int n=signupService.insertUser(signup);
        String msg=(n>0)?"회원가입 성공-로그인 페이지로 이동합니다":"회원가입 실패";
        String loc=(n>0)?"/":"javascript:history.back()";
        model.addAttribute("msg",msg);
        model.addAttribute("loc", loc);
        return "message";
    }
    @GetMapping("/idCheck")
    public String idCheck(){
        return "member/idCheck";
    }

    // Ajax로 아이디 중복 체크 요청을 처리하는 API
    @GetMapping("/checkId")
    @ResponseBody // JSON 반환
    public Map<String, Boolean> checkId(@RequestParam String memberId) {
        //log.info("아이디 중복 체크 요청: {}", memberId);

        // 서비스 계층을 호출하여 중복 여부 확인
        boolean isAvailable = !signupService.isIdExists(memberId); // 사용 가능한 아이디인지 확인
        Map<String, Boolean> result = new HashMap<>();
        result.put("isAvailable", isAvailable); // 결과를 JSON 형식으로 반환
        return result;
    }


    @GetMapping("/bSignup")
    public String bSignupForm(){

        return "member/bSignup";
    }

    @PostMapping("/bSignup")
    public String bSignupProcess(CompanyDTO signup, Model model, HttpServletRequest req, HttpSession ses) {
        // 유효성 체크
        if (signup.getCompanyNum() == null || signup.getCompanyNum().trim().isBlank()) {
            model.addAttribute("msg", "사업자 등록번호는 필수입니다.");
            model.addAttribute("loc", "/bSignup");
            return "message";
        }

        try {
            // 중복 확인
            boolean isExists = signupService.isCompanyExists(signup.getCompanyNum());
            if (isExists) {
                // 중복된 경우 기존 데이터 사용
                CompanyDTO existingCompany = signupService.findCompanyByCompanyNum(signup.getCompanyNum());
                if (existingCompany != null) {
                    if (existingCompany.getCompanyLogoName() != null && !existingCompany.getCompanyLogoName().isEmpty()) {
                        existingCompany.setCompanyLogoUrl("/company_logos/" + existingCompany.getCompanyLogoName());
                    } else {
                        existingCompany.setCompanyLogoUrl(""); // 기본 이미지 설정
                    }
                }

                // 세션에 기존 데이터 저장
                ses.setAttribute("companySignup", existingCompany);
                model.addAttribute("msg", "이미 등록된 사업자 번호입니다. 기존 정보를 사용하여 다음 단계로 이동합니다.");
                model.addAttribute("loc", "/bSignup2");
                return "message";
            }

            // 업로드 디렉토리 설정
            String uploadDir = req.getServletContext().getRealPath("/company_logos/");
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 파일 업로드 처리
            if (signup.getCompanyLogo() != null && !signup.getCompanyLogo().isEmpty()) {
                String originalFilename = signup.getCompanyLogo().getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String savedFilename = uuid + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

                File file = new File(uploadDir, savedFilename);
                signup.getCompanyLogo().transferTo(file);

                // 파일명과 URL 설정
                signup.setCompanyLogoName(savedFilename); // 파일명 저장
                signup.setCompanyLogoUrl("/company_logos/" + savedFilename); // URL 생성
            } else if (signup.getCompanyLogoUrl() != null && !signup.getCompanyLogoUrl().isEmpty()) {
                // URL만 전달된 경우
                signup.setCompanyLogoName(null); // 파일명 비워두기
                signup.setCompanyLogoUrl(signup.getCompanyLogoUrl());
            } else {
                // 기본값 설정
                signup.setCompanyLogoName("");
                signup.setCompanyLogoUrl("");
            }

            // 데이터베이스 저장
            int result = signupService.insertCompany(signup);
            if (result > 0) {
                ses.setAttribute("companySignup", signup);
            }

            String msg = (result > 0) ? "기업 회원가입 성공 - 다음 단계로 이동합니다." : "기업 회원가입 실패";
            String loc = (result > 0) ? "/bSignup2" : "javascript:history.back()";

            model.addAttribute("msg", msg);
            model.addAttribute("loc", loc);
            return "message";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msg", "오류 발생: " + e.getMessage());
            model.addAttribute("loc", "javascript:history.back()");
            return "message";
        }
    }

    @GetMapping("/bSignup2")
    public String BsignupForm2(HttpSession ses, Model model){
        CompanyDTO companySignup = (CompanyDTO) ses.getAttribute("companySignup");
        //log.info("companyinfo==={}",companySignup);

        if (companySignup == null) {
            model.addAttribute("msg", "이전 단계의 회사 정보가 없습니다. 다시 입력해주세요.");
            model.addAttribute("loc", "/bSignup");
            return "message"; // 에러 메시지 출력
        }

        CompanyDTO companyData = signupService.findCompanyByCompanyNum(companySignup.getCompanyNum());
        if (companyData == null) {
            model.addAttribute("msg", "해당 사업자등록번호에 해당하는 회사가 존재하지 않습니다. 다시 입력해주세요.");
            model.addAttribute("loc", "/bSignup");
            return "message"; // 에러 메시지 출력
        }

        // companyId를 companySignup 객체에 설정
        companySignup.setCompanyId(companyData.getCompanyId());

        // 세션 갱신
        ses.setAttribute("companySignup", companySignup);
        // 모델에 기업 정보 전달

        model.addAttribute("companyInfo", companySignup);
        log.info("compayInfo==={}",companySignup);
        return "member/bSignup2";
    }

    @PostMapping("/bSignup2")
    public String bSignupProcess2(SignupDTO signup,
                                  @RequestParam("memberImg") MultipartFile memberImg,
                                  HttpServletRequest req, HttpSession ses,
                                  Model model){
        //log.info("bsignup2====={}", signup);
        CompanyDTO companySignup = (CompanyDTO) ses.getAttribute("companySignup");
        if (companySignup == null || companySignup.getCompanyId() == null) {
            //log.error("companySignup is null or companyId is null in session.");
            model.addAttribute("msg", "회사 정보가 없습니다. 이전 단계로 돌아가 주세요.");
            model.addAttribute("loc", "/bSignup");
            return "message";
        }

        signup.setCompanyId(companySignup.getCompanyId());

        // 업로드 디렉토리 경로 설정
        String upDir = req.getServletContext().getRealPath("/member_img");
        File dir = new File(upDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 디렉토리 생성
        }

        // 파일 처리
        if (!memberImg.isEmpty() && memberImg.getOriginalFilename() != null) {
            try {
                String originalFileName = memberImg.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String newFileName = uuid + "_" + originalFileName.replaceAll("\\s+", "_");
                memberImg.transferTo(new File(upDir, newFileName)); // 저장
                signup.setMemberImgName(newFileName); // DTO에 파일 이름 설정
            } catch (IOException e) {
                //log.error("파일 업로드 실패: {}", e.getMessage());
                model.addAttribute("msg", "파일 업로드 중 오류가 발생했습니다.");
                model.addAttribute("loc", "/bSignup2");
                return "message";
            }
        } else {
            signup.setMemberImgName("images/noIdp.png"); // 기본 이미지 설정
        }

        boolean isValid = signupService.validateSignup(signup);

        if (!isValid) {
            model.addAttribute("msg", "입력한 정보에 오류가 있습니다. 다시 확인해주세요.");
            model.addAttribute("loc", "/bSignup2");
            return "message";
        }
        //서비스 객체의 메서드를 호출
        int n=signupService.insertCompanyUser(signup);
        String msg=(n>0)?"회원가입 성공-로그인 페이지로 이동합니다":"회원가입 실패";
        String loc=(n>0)?"/":"javascript:history.back()";
        model.addAttribute("msg",msg);
        model.addAttribute("loc", loc);
        return "message";
    }
}
