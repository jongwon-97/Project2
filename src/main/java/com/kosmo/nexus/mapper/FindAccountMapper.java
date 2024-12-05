package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.FindAccountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FindAccountMapper {
    FindAccountDTO findAccountByCompanyNum(@Param("companyNum") String companyNum, @Param("memberNum") String memberNum);
    FindAccountDTO findAccountByEmail(@Param("memberName") String memberName, @Param("memberEmail") String memberEmail);
    FindAccountDTO findAccountByPhone(@Param("memberName") String memberName, @Param("memberPhone") String memberPhone);

    FindAccountDTO findPasswordByEmail(@Param("memberId") String memberId,
                                       @Param("memberName") String memberName,
                                       @Param("memberEmail") String memberEmail);
    FindAccountDTO findPasswordByPhone(@Param("memberId") String memberId,
                                       @Param("memberName") String memberName,
                                       @Param("memberPhone") String memberPhone);
    void updatePassword(@Param("memberId") String memberId, @Param("newPassword") String newPassword);
}
