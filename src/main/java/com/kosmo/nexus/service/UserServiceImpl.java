package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.mapper.MemberMapper;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    private final MemberMapper memberMapper;
    public UserServiceImpl(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public MemberDTO findMember(String id){
        return memberMapper.findMember(id);
    }

    @Override
    public int updateMember(MemberDTO member) {
        return memberMapper.updateMember(member);
    }
}
