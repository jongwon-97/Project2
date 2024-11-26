package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.SignupDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SignupMapper {

    int insertCompany(SignupDTO signup);
    int insertUser(SignupDTO signup);
    int insertCompanyUser(SignupDTO signup);
    SignupDTO findCompanyByCompanyNum (String companyNum);
    int countById(String memberId);
}
