package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.SignupDTO;
import org.springframework.stereotype.Service;


@Service
public interface SignupService {
    int insertUser (SignupDTO signup);
    boolean validateSignup(SignupDTO signup);
    int insertCompany (SignupDTO signup);
    int insertCompanyUser(SignupDTO signup);
    SignupDTO findCompanyByCompanyNum (String companyNum);
    boolean isIdExists(String memberId);
}
