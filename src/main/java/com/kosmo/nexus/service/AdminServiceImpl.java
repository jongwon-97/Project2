package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.dto.SignupDTO;
import com.kosmo.nexus.mapper.MemberMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    public List<String> findDepartmentByCompanyId(Long companyId) {
        return memberMapper.findDepartmentByCompanyId(companyId);
    }

    @Override
    public List<String> findRankByCompanyId(Long companyId) {
        return memberMapper.findRankByCompanyId(companyId);
    }

    @Override
    public List<MemberDTO> searchMemberList(String search, String option, Long companyId) {
        return memberMapper.searchMemberList(search, option, companyId);
    }

    @Override
    public List<MemberDTO> searchMemberListByDate(Map<String, Object> params) {
        return memberMapper.searchMemberListByDate(params);
    }

    @Override
    public int updateMemberByAdmin(MemberDTO member, Long companyId) {
        return memberMapper.updateMemberByAdmin(member, companyId);
    }

    @Override
    public int insertMemberList(List<SignupDTO> members) {
        return memberMapper.insertMemberList(members);
    }

    @Override
    public List<String> findImgNamebyIdList(List<String> memberIds, Long companyId) {
        return memberMapper.findImgNamebyIdList(memberIds, companyId);
    }

    @Override
    public int deleteMemberList(List<String> memberIds, Long companyId) {
        return memberMapper.deleteMemberList(memberIds, companyId);
    }

    @Override
    public MemberDTO findMemberWithCompanyId(String memberId, Long companyId){
        return memberMapper.findMemberWithCompanyId(memberId, companyId);
    }


}
