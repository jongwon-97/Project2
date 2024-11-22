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
    public List<MemberDTO> findMemberList(int companyId) {
        return memberMapper.findMemberList(companyId);
    }

    @Override
    public int deleteMemberList(List<String> membersDel) {
        return memberMapper.deleteMemberList(membersDel);
    }
}
