package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.dto.SignupDTO;
import com.kosmo.nexus.mapper.MemberMapper;
import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService{
    private final MemberMapper memberMapper;

    @Autowired
    private ServletContext servletContext;

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
    public int updateMemberDepartment(List<String> memberIds, String selDepartment, Long companyId) {
        return memberMapper.updateMemberDepartment(memberIds, selDepartment, companyId);
    }

    @Override
    public int updateMemberRank(List<String> memberIds, String selRank, Long companyId) {
        return memberMapper.updateMemberRank(memberIds, selRank, companyId);
    }

    @Override
    public int updateMemberStatus(List<String> memberIds, String selStatus, Long companyId) {
        return memberMapper.updateMemberStatus(memberIds, selStatus, companyId);
    }

    @Override
    public List<String> findImgNamebyIdList(List<String> memberIds, Long companyId) {
        return memberMapper.findImgNamebyIdList(memberIds, companyId);
    }

    @Override
    public boolean isMemberNumDuplicate(String memberNum, Long companyId) {
        return memberMapper.findMemberNumByCompanyId(memberNum, companyId) > 0;

    }

    @Override
    public String findImagePathByMember(String memberNum, Long companyId) {
        return memberMapper.findImagePathByMember(memberNum, companyId);
    }

    @Override
    public void updateMemberImage(String memberNum, Long companyId, String newImagePath) {
        // 기존 이미지 경로 조회
        String oldImagePath = findImagePathByMember(memberNum, companyId);

        // 기존 이미지 삭제
        if (oldImagePath != null && !oldImagePath.isEmpty()) {
            try {
                deleteImage(oldImagePath);
            } catch (IOException e) {
                log.error("기존 이미지 삭제 실패: {}", e.getMessage());
            }
        }

        // DB에 새 이미지 경로 업데이트
        memberMapper.updateMemberImage(memberNum, companyId, newImagePath);
    }

    private void deleteImage(String imagePath) throws IOException {
        Path path = Paths.get(servletContext.getRealPath(imagePath));
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("이미지 파일 삭제됨: {}", imagePath);
        } else {
            log.warn("이미지 파일을 찾을 수 없음: {}", imagePath);
        }
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
