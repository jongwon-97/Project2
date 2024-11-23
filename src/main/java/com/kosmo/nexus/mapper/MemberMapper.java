package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberMapper {
    MemberDTO findMember(String memberId);
    MemberDTO findMemberWithCompanyId(String memberId, Long companyId);
    List<MemberDTO> findMemberList (Long companyId);
    int updateMember(MemberDTO member);
    int deleteMemberList(List<String> membersDel);
    Long findCompanyIdByMemberId(String memberId);
}
