package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.FindAccountDTO;


public interface FindAccountService {
    FindAccountDTO findAccountByCompanyNum(String companyNum, String memberNum);
    FindAccountDTO findAccountByEmail(String memberName, String memberEmail);
    FindAccountDTO findAccountByPhone(String memberName, String memberPhone);

    // 비밀번호 찾기
    FindAccountDTO findPasswordByEmail(String memberId, String memberName, String memberEmail);
    FindAccountDTO findPasswordByPhone(String memberId, String memberName, String memberPhone);
    FindAccountDTO findPasswordByCompanyNum(String memberId, String companyNum, String memberNum);

    void updatePassword(String memberId, String newPassword);
}
