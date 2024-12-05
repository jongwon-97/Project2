package com.kosmo.nexus.controller;

import com.kosmo.nexus.dto.TestDTO;
import com.kosmo.nexus.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Slf4j
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/test")
    public String test(){
        return "/test";
    }

    @GetMapping("/test/insert")
    public String insertTest(){
        TestDTO test = new TestDTO();
        test.setId("test5");
        test.setTitle("입력하기 확인중");
        int result = testService.insertTest(test);
        log.info("입력결과 == {}", result);
        return "/test";
    }

    @GetMapping("/test/select")
    public String selectTest(){
        List<TestDTO> result = testService.listTest();

        log.info("출력결과==={}", result);
        return "/test";
    }

}
