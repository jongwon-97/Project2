package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.FindAccountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FindAccountMapper {
    FindAccountDTO findPersonalAccountByEmail(@Param("memberName") String memberName, @Param("memberEmail") String memberEmail);
    FindAccountDTO findPersonalAccountByPhone(@Param("memberName") String memberName, @Param("memberPhone") String memberPhone);
    FindAccountDTO findBusinessAccountByCompanyNum(@Param("companyNum") String companyNum, @Param("memberNum") String memberNum);
    FindAccountDTO findBusinessAccountByEmail(@Param("memberName") String memberName, @Param("memberEmail") String memberEmail);
    FindAccountDTO findBusinessAccountByPhone(@Param("memberName") String memberName, @Param("memberPhone") String memberPhone);
}
