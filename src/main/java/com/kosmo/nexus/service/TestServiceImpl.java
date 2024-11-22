package com.kosmo.nexus.service;

import com.kosmo.nexus.dto.TestDTO;
import com.kosmo.nexus.mapper.TestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TestServiceImpl implements TestService{
    private final TestMapper testMapper;

    public TestServiceImpl(TestMapper testMapper) {
        this.testMapper = testMapper;
    }

    @Override
    public int insertTest(TestDTO test){
        return testMapper.insertTest(test);
    }

    @Override
    public List<TestDTO> listTest(){
        return testMapper.listTest();
    }
}
