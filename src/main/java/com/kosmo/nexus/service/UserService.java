package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.MemberDTO;


public interface UserService {

    MemberDTO findMember(String id);
    int updateMember(MemberDTO member);
}
