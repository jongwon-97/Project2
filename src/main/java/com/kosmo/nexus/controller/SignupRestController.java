package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.CompanyDTO;
import com.kosmo.nexus.service.SignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company")
public class SignupRestController {

    @Autowired
    private SignupService signupService;

    @GetMapping("/{companyNum}")
    public ResponseEntity<?> getCompanyByCompanyNum(@PathVariable String companyNum) {
        try {
            if (companyNum == null || companyNum.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("사업자 등록번호를 입력하세요.");
            }

            CompanyDTO company = signupService.findCompanyByCompanyNum(companyNum);
            if (company != null) {
                // 로고 경로 추가
                if (company.getCompanyLogoName() != null && !company.getCompanyLogoName().isEmpty()) {
                    String logoUrl = "/company_logos/" + company.getCompanyLogoName(); // URL 생성
                    company.setCompanyLogoUrl(logoUrl); // URL 설정
                }
                return ResponseEntity.ok(company);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사업자등록번호로 등록된 기업 정보가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생: " + e.getMessage());
        }
    }
}
