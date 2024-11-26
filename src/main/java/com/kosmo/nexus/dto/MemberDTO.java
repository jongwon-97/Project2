package com.kosmo.nexus.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;

@NoArgsConstructor  //기본 생성자
//@AllArgsConstructor //인자 생성자 (거의 안씀)
@Setter //setxxx()
@Getter //getxxx()
@ToString //toString() 오버라이드
public class MemberDTO {
    private String memberId;           //아이디
    private String memberName;         //이름
    private String memberRole;         //권한
    private String memberImgName;          //이미지
    private int memberSns;             //sns 번호 (api 연동용 아닐때 0 구글 1 네이버 2 카카오 3)

    private String memberEmail;        //이메일
    private String memberPhone;        //전화번호
    private String memberGender;       //성별

    private Date memberBirth;        //생년월일

    private int memberPostcode;        //우편번호  (api 연동용 우편번호 int)
    private String memberAddress;      //주소
    private String memberDAddress;    //상세 주소

    private String memberNum;             //사번
    private String memberRank;         //직급
    private String memberDepartment;   //부서
    private Long companyId;             //회사 ID

    private String memberStatus;       //상태

    private Date memberStartDate;   //회원생성일
    private Date memberLastDate;    //마지막 접속일
}