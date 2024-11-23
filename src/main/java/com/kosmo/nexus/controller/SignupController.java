package com.kosmo.nexus.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class SignupController {

    @GetMapping("/bSignup")
    public String bSignupForm(){
        log.info("btest");
        return "member/bSignup";
    }

    @GetMapping("/pSignup")
    public String pSignupForm(){

        return "member/pSignup";
    }

}
