package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.CompanyDTO;

import java.util.List;

public interface DevService {
    List<CompanyDTO> findCompanyList();
}
