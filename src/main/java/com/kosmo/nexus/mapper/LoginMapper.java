package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.LoginDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginMapper {

    //List<LoginDTO> listMember();
    LoginDTO findMemberByUserId(String memberId);

    void updateMemberStatus(@Param("memberId") String memberId, @Param("status") String status);

}