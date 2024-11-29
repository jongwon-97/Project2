package com.kosmo.nexus.service;

import com.kosmo.nexus.common.util.PasswordUtil;
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

    //기업회원 아이디 찾기 로직
    @Override
    public FindAccountDTO findAccountByCompanyNum(String companyNum, String memberNum) {
        return findAccountMapper.findAccountByCompanyNum(companyNum, memberNum);
    }


    @Override
    public FindAccountDTO findAccountByEmail(String memberName, String memberEmail) {
        FindAccountDTO result = findAccountMapper.findAccountByEmail(memberName, memberEmail);
        return result;
    }

    @Override
    public FindAccountDTO findAccountByPhone(String memberName, String memberPhone) {
        FindAccountDTO result = findAccountMapper.findAccountByPhone(memberName, memberPhone);
        return result;
    }

    @Override
    public FindAccountDTO findPasswordByEmail(String memberId, String memberName, String memberEmail) {
        log.info("findPasswordByEmail called with memberId={}, memberName={}, memberEmail={}", memberId, memberName, memberEmail);
        return findAccountMapper.findPasswordByEmail(memberId, memberName, memberEmail);
    }

    @Override
    public FindAccountDTO findPasswordByPhone(String memberId, String memberName, String memberPhone) {
        log.info("findPasswordByPhone called with memberId={}, memberName={}, memberPhone={}", memberId, memberName, memberPhone);
        return findAccountMapper.findPasswordByPhone(memberId, memberName, memberPhone);
    }


    @Override
    public FindAccountDTO findPasswordByCompanyNum(String memberId, String companyNum, String memberNum) {
        log.info("findPasswordByCompanyNum called with memberId={}, companyNum={}, memberNum={}", memberId, companyNum, memberNum);
        return findAccountMapper.findAccountByCompanyNum(companyNum, memberNum);
    }

    @Override
    public void updatePassword(String memberId, String newPassword) {
        log.info("updatePassword called with memberId={}, newPassword={}", memberId, newPassword);
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        findAccountMapper.updatePassword(memberId, hashedPassword);
        log.info("updatePassword called with memberId={}, newPassword={}", memberId, newPassword);
    }
}
