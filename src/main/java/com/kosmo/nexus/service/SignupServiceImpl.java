package com.kosmo.nexus.service;

import com.kosmo.nexus.common.util.PasswordUtil;
import com.kosmo.nexus.dto.CompanyDTO;
import com.kosmo.nexus.dto.SignupDTO;
import com.kosmo.nexus.mapper.SignupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;


@Service
@Slf4j
public class SignupServiceImpl implements SignupService{

    private final SignupMapper signupMapper;


    public SignupServiceImpl(SignupMapper signupMapper) {
        this.signupMapper = signupMapper;

    }


    @Override
    public int insertUser(SignupDTO signup) {
        String hashedPassword = PasswordUtil.hashPassword(signup.getMemberPw());
        signup.setMemberPw(hashedPassword);

        return signupMapper.insertUser(signup);
    }

    @Override
    public boolean validateSignup(SignupDTO signup) {
        // 필수 필드 값 확인 (아이디 포함)
        if (isBlank(signup.getMemberId()) || isBlank(signup.getMemberPw()) ||
                isBlank(signup.getMemberEmail()) || isBlank(signup.getMemberName()) ||
                isBlank(signup.getMemberPhone()) || signup.getMemberBirth() == null ||
                signup.getMemberPostcode() == 0|| isBlank(signup.getMemberAddress())){
            return false; // 필수 항목이 누락된 경우
        }

        // 정규식 검증
        return  isValidPassword(signup.getMemberPw()) &&
                isValidEmail(signup.getMemberEmail()) &&
                isValidPhone(signup.getMemberPhone());
    }

    private boolean isValidPostcode(int memberPostcode) {
        // 우편번호는 양수여야 함 (예: 12345)
        return memberPostcode > 0;
    }

    private boolean isBlank(Date memberBirth) {
        Date today = new Date(System.currentTimeMillis());
        return !memberBirth.after(today);
    }

    // 공백 및 null 확인
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
        //String regex = "^\\d{10,11}$"; // 숫자만, 10~11자리
        String regex = "^(01[0|1|6|7|8|9]-\\d{3,4}-\\d{4}|02-\\d{3,4}-\\d{4}|0[3-6][1-9]-\\d{3,4}-\\d{4}|050-\\d{4}-\\d{4}|050\\d-\\d{4}-\\d{4})$";
        //String regexWithHyphen = "^01[0|1|6|7|8|9]-\\d{3,4}-\\d{4}$";
        return memberPhone.matches(regex);
    }

    @Override
    public int insertCompany(CompanyDTO signup) {
        return signupMapper.insertCompany(signup);
    }

    @Override
    public int insertCompanyUser(SignupDTO signup) {
        String hashedPassword = PasswordUtil.hashPassword(signup.getMemberPw());
        signup.setMemberPw(hashedPassword);
        log.info("Validating SignupDTO: {}", signup);
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

    @Override
    public boolean isCompanyExists(String companyNum) {
        CompanyDTO existingCompany = signupMapper.findCompanyByCompanyNum(companyNum);
        return existingCompany != null; // null이 아니면 이미 존재
    }

}
