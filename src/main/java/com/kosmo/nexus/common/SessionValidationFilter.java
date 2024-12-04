package com.kosmo.nexus.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SessionValidationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;
        HttpSession session = httpRequest.getSession(false);

        // 브라우저 캐시 방지
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        // 세션 유지가 필요한 URL 경로 설정
        String requestURI = httpRequest.getRequestURI();
        if (session != null && (requestURI.equals("/dev/home") || requestURI.equals("/admin/home") || requestURI.equals("/user/home"))) {
            // 세션이 유지 중이라면 필터 체인 통과
            chain.doFilter(req, res);
        } else {
            // 기타 URL은 일반 처리
            chain.doFilter(req, res);
        }
    }
}