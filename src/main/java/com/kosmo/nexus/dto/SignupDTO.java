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
public class SignupDTO {
    private String memberId;           //아이디
    private String memberPw;           //비밀번호
    private String memberName;         //이름
    private String memberRole;         //권한
    private MultipartFile memberImg;          //이미지
    private String memberImgName;      // 저장된 이미지 파일 이름 (추가)
    private int memberSns;             //sns 번호 (api 연동용 아닐때 0 구글 1 네이버 2 카카오 3)

    private String memberEmail;        //이메일
    private String memberPhone;        //전화번호
    private String memberGender;       //성별
    private Date memberBirth;        //생년월일

    private Integer memberPostcode;        //우편번호  (api 연동용 우편번호 int)
    private String memberAddress;      //주소
    private String memberDAddress;    //상세 주소

    private String memberNum;             //사번
    private String memberRank;         //직급
    private String memberDepartment;   //부서
    private Long companyId;             //회사 ID

    private String memberStatus;       //상태

    private Date memberStartDate;   //회원생성일
    private Date memberLastDate;    //마지막 접속일

    private String companyNum;        //사업자 등록번호

    private String companyName;       //회사 명
    private String companyEmail;      //회사 이메일
    private String companyPhone;      //회사 전화번호

    private String companyPostcode;   //우편번호
    private String companyAddress;    //대표주소
    private MultipartFile companyLogo;       // 회사 로고 이미지 파일
    private String companyLogoName;      // 파일 이름을 저장할 String 필드 추가

}
