package com.kosmo.nexus.common.interceptor;

import com.kosmo.nexus.dto.LoginDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class DevCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res,
                             Object handler)
            throws ServletException, IOException {
        HttpSession ses = req.getSession();
        LoginDTO loginUser = (LoginDTO) ses.getAttribute("loginUser");
        if(loginUser==null || !loginUser.getMemberRole().equals("Dev")){
            res.setContentType("text/html; charset=UTF-8");  // 응답의 콘텐츠 타입 설정
            res.getWriter().write("<script type=\"text/javascript\">"
                    + "alert('개발자만 이용 가능합니다.');"
                    + "history.back();"
                    + "</script>");
            return false;  // 인터셉터에서 false를 반환하여 컨트롤러로 넘어가지 않도록 함
        }// ------------------
        return true;

    }
}
