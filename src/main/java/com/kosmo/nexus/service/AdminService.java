package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.MemberDTO;

import java.util.List;

public interface AdminService {
    Long findCompanyIdByMemberId(String memberId);

    List<MemberDTO> findMemberList(Long companyId);
    int deleteMemberList(List<String> membersDel);
    MemberDTO findMemberWithCompanyId(String memberId, Long companyId);
}
