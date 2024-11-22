package com.kosmo.nexus.mapper;

import com.kosmo.nexus.dto.TestDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestMapper {
    int insertTest(TestDTO test);
    List<TestDTO> listTest();

}
