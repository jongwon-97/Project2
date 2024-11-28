package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.FindAccountDTO;
import com.kosmo.nexus.mapper.FindAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FindAccoutServiceImpl implements FindAccountService{

    private final FindAccountMapper findAccountMapper;
    //개인회원 아이디 찾기 로직
    @Override
    public FindAccountDTO findPersonalAccountByEmail(String memberName, String memberEmail) {
        FindAccountDTO result = findAccountMapper.findPersonalAccountByEmail(memberName, memberEmail);
        return result;
    }

    @Override
    public FindAccountDTO findPersonalAccountByPhone(String memberName, String memberPhone) {
        FindAccountDTO result = findAccountMapper.findPersonalAccountByPhone(memberName, memberPhone);
        return result;
    }

    //기업회원 아이디 찾기 로직
    @Override
    public FindAccountDTO findBusinessAccountByCompanyNum(String companyNum, String memberNum) {
        log.info("findBusinessAccountByCompanyNum called with companyNum='{}', memberNum='{}'", companyNum, memberNum);
        return findAccountMapper.findBusinessAccountByCompanyNum(companyNum, memberNum);
    }


    @Override
    public FindAccountDTO findBusinessAccountByEmail(String memberName, String memberEmail) {
        log.info("findBusinessAccountByEmail called with memberName='{}', memberEmail='{}'", memberName, memberEmail);
        FindAccountDTO result = findAccountMapper.findBusinessAccountByEmail(memberName, memberEmail);
        return result;
    }

    @Override
    public FindAccountDTO findBusinessAccountByPhone(String memberName, String memberPhone) {
        log.info("findBusinessAccountByPhone called with memberName='{}', memberPhone='{}'", memberName, memberPhone);
        FindAccountDTO result = findAccountMapper.findBusinessAccountByPhone(memberName, memberPhone);
        return result;
    }
}
