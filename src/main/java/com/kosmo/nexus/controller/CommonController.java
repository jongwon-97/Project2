package com.kosmo.nexus.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class CommonController {

    @GetMapping("/message")
    public String showMessage(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
        // Interceptor에서 설정한 값을 가져옵니다.
        String msg = (String) request.getAttribute("msg"); // "msg" 속성값 가져오기
        String loc = (String) request.getAttribute("loc");
        log.info("msg==={}, loc==={}", msg, loc);

        // Model에 전달하여 view에서 사용하도록 설정
        model.addAttribute("msg", msg);
        model.addAttribute("loc", loc);
        return "message";  // message.html을 템플릿으로 사용
    }
}
