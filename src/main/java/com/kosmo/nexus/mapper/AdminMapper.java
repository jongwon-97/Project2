package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMapper {
    List<MemberDTO> findMemberList(int companyId);
}
