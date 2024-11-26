package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.CompanyDTO;
import com.kosmo.nexus.dto.SignupDTO;
import org.springframework.stereotype.Service;


@Service
public interface SignupService {
    int insertUser (SignupDTO signup);
    boolean validateSignup(SignupDTO signup);
    int insertCompany (CompanyDTO signup);
    int insertCompanyUser(SignupDTO signup);
    CompanyDTO findCompanyByCompanyNum (String companyNum);
    boolean isIdExists(String memberId);
}