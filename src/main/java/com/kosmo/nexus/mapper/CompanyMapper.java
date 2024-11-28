package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.CompanyDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CompanyMapper {
    List<CompanyDTO> findCompanyList();
}
