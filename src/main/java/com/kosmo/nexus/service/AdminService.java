package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.dto.SignupDTO;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Long findCompanyIdByMemberId(String memberId);
    List<MemberDTO> findMemberList(Long companyId);
    List<String> findDepartmentByCompanyId(Long companyId);
    List<String> findRankByCompanyId(Long companyId);
    List<MemberDTO> searchMemberList(String search, String option, Long companyId);
    List<MemberDTO> searchMemberListByDate(Map<String, Object> params);
    int deleteMemberList(List<String> memberIds, Long companyId);
    MemberDTO findMemberWithCompanyId(String memberId, Long companyId);
    int updateMemberByAdmin(MemberDTO member, Long companyId);
    int insertMemberList(List<SignupDTO> members);
    List<String> findImgNamebyIdList(List<String> memberIds, Long companyId);
}
