package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.MemberDTO;
import com.kosmo.nexus.dto.SignupDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface MemberMapper {
    MemberDTO findMember(String memberId);
    MemberDTO findMemberWithCompanyId(String memberId, Long companyId);
    List<String> findDepartmentByCompanyId(Long companyId);
    List<String> findRankByCompanyId(Long companyId);
    List<MemberDTO> findMemberList (Long companyId);
    List<MemberDTO> searchMemberList(String search, String option, Long companyId);
    List<MemberDTO> searchMemberListByDate(Map<String, Object> params);
    int updateMember(MemberDTO member);
    int updateMemberByAdmin(MemberDTO member, Long companyId);
    int deleteMemberList(List<String> memberIds, Long companyId);
    int updateMemberDepartment(List<String> memberIds, String selDepartment, Long companyId);
    int updateMemberRank(List<String> memberIds, String selRank, Long companyId);
    int updateMemberStatus(List<String> memberIds, String selStatus, Long companyId);
    Long findCompanyIdByMemberId(String memberId);
    int insertMemberList(List<SignupDTO> members);
    List<String> findImgNamebyIdList(List<String> memberIds, Long companyId);

    int findMemberNumByCompanyId(String memberNum, Long companyId);
    String findImagePathByMember(String memberNum, Long companyId);
    int updateMemberImage(String memberNum, Long companyId, String imagePath);
}
