package com.kosmo.nexus.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class FindAccountDTO {

    private Long companyId;             //회사 ID
    private String companyNum;              //사업자 등록번호
    private String memberNum;             //사번

    private String memberId;           //아이디
    private String memberPw;           //비밀번호

    private String memberName;         //이름
    private String memberPhone;        //전화번호
    private String memberEmail;        //이메일
}
