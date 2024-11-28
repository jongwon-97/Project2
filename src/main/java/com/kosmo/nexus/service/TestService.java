package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.TestDTO;

import java.util.List;

public interface TestService {

    int insertTest(TestDTO test);
    List<TestDTO> listTest();
}
