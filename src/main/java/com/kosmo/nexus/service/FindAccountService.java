package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.FindAccountDTO;

public interface FindAccountService {
    FindAccountDTO findPersonalAccountByEmail(String memberName, String memberEmail);
    FindAccountDTO findPersonalAccountByPhone(String memberName, String memberPhone);
    FindAccountDTO findBusinessAccountByCompanyNum(String companyNum, String memberNum);
    FindAccountDTO findBusinessAccountByEmail(String memberName, String memberEmail);
    FindAccountDTO findBusinessAccountByPhone(String memberName, String memberPhone);
}
