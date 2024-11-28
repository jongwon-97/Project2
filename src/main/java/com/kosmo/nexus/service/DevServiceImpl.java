package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.CompanyDTO;
import com.kosmo.nexus.mapper.CompanyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DevServiceImpl implements DevService{

    private final CompanyMapper companyMapper;

    public DevServiceImpl(CompanyMapper companyMapper) {
        this.companyMapper = companyMapper;
    }

    @Override
    public List<CompanyDTO> findCompanyList() {
        return companyMapper.findCompanyList();
    }
}
