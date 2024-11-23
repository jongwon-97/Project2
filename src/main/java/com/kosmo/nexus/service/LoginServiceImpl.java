package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.LoginDTO;
import com.kosmo.nexus.common.exception.NoMemberException;
import com.kosmo.nexus.mapper.LoginMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService{

    private final LoginMapper loginMapper;

    @Autowired
    public LoginServiceImpl(LoginMapper loginMapper) {
        this.loginMapper = loginMapper;
    }

    @Override
    public List<LoginDTO> listMember() {
        return List.of();
    }

    @Override
    public LoginDTO findMemberByUserId(String memberId) {
        return null;
    }

    @Override
    public boolean idCheck(String memberId) {
        LoginDTO user = loginMapper.findMemberByUserId(memberId);

        if(user != null){
            return false;
        }

        return true;
    }

    @Override
    public LoginDTO loginCheck(LoginDTO tmpUser) throws NoMemberException {
        // 1. 회원 정보 조회
        LoginDTO member = loginMapper.findMemberByUserId(tmpUser.getMemberId());

        // 2. 회원 정보 검증
        if (member == null || !member.getMemberPw().equals(tmpUser.getMemberPw())) {
            throw new NoMemberException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        // 3. Role 검증 (대소문자 무시, 공백 제거)
        log.info("로그인한 사용자의 Role: {}", member.getMemberRole());
        if (!"User".equalsIgnoreCase(member.getMemberRole().trim())) {
            throw new NoMemberException("개인 회원이 아닙니다. 기업 회원은 기업회원 로그인 페이지를 이용해주세요.");
        }

        // 4. companyId 검증
        if (member.getCompanyId() != -1) {
            throw new NoMemberException("개인 회원이 아닙니다. 기업 회원은 기업회원 로그인 페이지를 이용해주세요.");
        }

        // 모든 검증 통과 시 회원 정보 반환
        return member;
    }

    @Override
    public LoginDTO businessLoginCheck(LoginDTO tmpUser) throws NoMemberException {
        // 회원 정보 조회
        LoginDTO member = loginMapper.findMemberByUserId(tmpUser.getMemberId());

        // 1. 회원 존재 및 비밀번호 확인
        if (member == null || !member.getMemberPw().equals(tmpUser.getMemberPw())) {
            throw new NoMemberException("아이디 또는 비밀번호가 틀렸습니다.");
        }
        log.info("role===={}", member.getMemberRole());

        // 2. 기업 회원 여부 확인 (memberRole 검증)
        if (!List.of("User", "Admin", "Dev").stream()
                .anyMatch(role -> role.equalsIgnoreCase(member.getMemberRole()))) {
            throw new NoMemberException("개인회원 계정입니다. 개인회원은 개인회원 로그인 페이지를 이용해주세요.");
        }

        // 3. 회사 ID 검증 (기업 회원 여부)
        if (member.getCompanyId() < 0) {
            throw new NoMemberException("개인회원 계정입니다. 개인회원은 개인회원 로그인 페이지를 이용해주세요.");
        }

        // 4. 상태 검증
        if ("정지".equalsIgnoreCase(member.getMemberStatus())) {
            throw new NoMemberException("정지된 계정입니다. 관리자에게 문의하세요.");
        }

        if ("휴면".equalsIgnoreCase(member.getMemberStatus())) {
            // 휴면 상태를 활동 상태로 변경
            loginMapper.updateMemberStatus(member.getMemberId(), "활동");
            log.info("휴면 계정 활성화: {}", member.getMemberId());
            member.setMemberStatus("활동");
        }

        // 4. 비즈니스 로직 처리 후 반환
        return member;
    }

}
