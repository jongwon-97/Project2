package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.CompanyDTO;
import com.kosmo.nexus.dto.SignupDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SignupMapper {

    int insertCompany(CompanyDTO signup);
    int insertUser(SignupDTO signup);
    int insertCompanyUser(SignupDTO signup);
    CompanyDTO findCompanyByCompanyNum (String companyNum);
    int countById(String memberId);
}
