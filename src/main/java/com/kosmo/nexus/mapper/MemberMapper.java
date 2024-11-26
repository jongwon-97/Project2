package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.dto.SignupDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberMapper {
    MemberDTO findMember(String memberId);
    MemberDTO findMemberWithCompanyId(String memberId, Long companyId);
    List<MemberDTO> findMemberList (Long companyId);
    List<MemberDTO> searchMemberList(String search, String option, Long companyId);
    int updateMember(MemberDTO member);
    int updateMemberByAdmin(MemberDTO member, Long companyId);
    int deleteMemberList(List<String> memberIds, Long companyId);
    Long findCompanyIdByMemberId(String memberId);
    int insertMemberList(List<SignupDTO> members);
    List<String> findImgNamebyIdList(List<String> memberIds, Long companyId);



}
