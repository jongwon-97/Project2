package com.kosmo.nexus;

import com.kosmo.nexus.common.interceptor.AdminCheckInterceptor;
import com.kosmo.nexus.common.interceptor.DevCheckInterceptor;
import com.kosmo.nexus.common.interceptor.UserCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AdminCheckInterceptor adminInterceptor;
    private final UserCheckInterceptor userInterceptor;
    private final DevCheckInterceptor devInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 관리자 체크 인터셉터 등록
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/user/**")   //권한이 필요한 패턴
                .order(1);//우선순위

        // 2. 관리자 체크 인터셉터 등록
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")   //권한이 필요한 패턴
                .order(2);//우선순위

        // 3. 개발자 체크 인터셉터 등록
        registry.addInterceptor(devInterceptor)
                .addPathPatterns("/dev/**")   //권한이 필요한 패턴
                .order(3);//우선순위
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/company_logos/**")
                .addResourceLocations("file:src/main/webapp/company_logos/");
    }
}
