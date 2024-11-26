package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.CompanyDTO;
import com.kosmo.nexus.dto.SignupDTO;
import com.kosmo.nexus.mapper.SignupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SignupServiceImpl implements SignupService{

    private final SignupMapper signupMapper;

    public SignupServiceImpl(SignupMapper signupMapper) {
        this.signupMapper = signupMapper;
    }

    @Override
    public int insertUser(SignupDTO signup) {
        return signupMapper.insertUser(signup);
    }

    @Override
    public boolean validateSignup(SignupDTO signup) {
        // 유효성 검증 (null 또는 빈 값 확인)
        if (signup.getMemberId() == null || signup.getMemberId().trim().isBlank() ||
                signup.getMemberPw() == null || signup.getMemberPw().trim().isBlank() ||
                signup.getMemberEmail() == null || signup.getMemberEmail().trim().isBlank() ||
                signup.getMemberName() == null || signup.getMemberName().trim().isBlank() ||
                signup.getMemberPhone() == null || signup.getMemberPhone().trim().isBlank()) {
            return false; // 필수 항목이 누락됨
        }

        // 정규식 검증 (아이디, 비밀번호, 이메일, 전화번호)
        if (!isValidMemberId(signup.getMemberId()) || !isValidPassword(signup.getMemberPw()) ||
                !isValidEmail(signup.getMemberEmail()) || !isValidPhone(signup.getMemberPhone())) {
            return false; // 검증 실패
        }

        return true; // 모든 검증 통과
    }

    // 아이디 유효성 검증
    private boolean isValidMemberId(String memberId) {
        String regex = "^[a-zA-Z0-9]{6,12}$";  // 알파벳, 숫자 조합, 6~12자
        return memberId.matches(regex);
    }

    // 비밀번호 유효성 검증
    private boolean isValidPassword(String memberPw) {
        String regex = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        // 최소 8자, 소문자, 숫자, 특수문자 포함
        return memberPw.matches(regex);
    }

    // 이메일 유효성 검증
    private boolean isValidEmail(String memberEmail) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        // 이메일 형식에 맞는 정규식
        return memberEmail.matches(regex);
    }

    // 전화번호 유효성 검증
    private boolean isValidPhone(String memberPhone) {
        String regex = "^\\d{10,11}$"; // 숫자만, 10~11자리
        return memberPhone.matches(regex);
    }

    @Override
    public int insertCompany(CompanyDTO signup) {
        return signupMapper.insertCompany(signup);
    }

    @Override
    public int insertCompanyUser(SignupDTO signup) {
        return signupMapper.insertCompanyUser(signup);
    }

    @Override
    public CompanyDTO findCompanyByCompanyNum(String companyNum) {
        return signupMapper.findCompanyByCompanyNum(companyNum);
    }

    @Override
    public boolean isIdExists(String memberId) {
        int count = signupMapper.countById(memberId);
        return count > 0; // 0보다 크면 중복된 아이디
    }

}
