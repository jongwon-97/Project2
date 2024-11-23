package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.mapper.MemberMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService{
    private final MemberMapper memberMapper;

    public AdminServiceImpl(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public Long findCompanyIdByMemberId(String memberId){
        return memberMapper.findCompanyIdByMemberId(memberId);
    }

    @Override
    public List<MemberDTO> findMemberList(Long companyId) {
        return memberMapper.findMemberList(companyId);
    }

    @Override
    public int deleteMemberList(List<String> membersDel) {
        return memberMapper.deleteMemberList(membersDel);
    }

    @Override
    public MemberDTO findMemberWithCompanyId(String memberId, Long companyId){
        return memberMapper.findMemberWithCompanyId(memberId, companyId);
    }
}
