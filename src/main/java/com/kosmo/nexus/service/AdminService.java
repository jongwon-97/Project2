package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.MemberDTO;

import java.util.List;

public interface AdminService {
    List<MemberDTO> findMemberList(int companyId);
    int deleteMemberList(List<String> membersDel);
}
