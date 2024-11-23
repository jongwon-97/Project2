package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.exception.NoMemberException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LoginService {

    List<LoginDTO> listMember();
    LoginDTO findMemberByUserId(String memberId);
    boolean idCheck(String memberId);
    LoginDTO loginCheck(LoginDTO tmpUser) throws NoMemberException;

    LoginDTO businessLoginCheck(LoginDTO tmpUser) throws NoMemberException;
}
