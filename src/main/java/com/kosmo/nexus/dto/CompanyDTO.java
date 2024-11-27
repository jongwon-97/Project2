package com.kosmo.nexus.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class CompanyDTO {
    private Long companyId;                 //회사 ID
    private String companyNum;              //사업자 등록번호

    private String companyName;             //회사 명
    private String companyEmail;            //회사 이메일
    private String companyPhone;            //회사 전화번호

    private String companyPostcode;         //우편번호
    private String companyAddress;          //대표주소
    private String companyDAddress;          //회사 상세주소

    private MultipartFile companyLogo;      //회사 로고 이미지 파일
    private String companyLogoName;         //파일 이름을 저장할 필드
    private String companyLogoUrl;          // 로고 이미지 경로 (URL)
    private Date companyStartDate;          //회사 가입일
}
