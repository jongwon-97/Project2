package com.kosmo.nexus;

import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.JspConfigDescriptorImpl;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroup;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroupDescriptorImpl;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ServletInitializer extends SpringBootServletInitializer {

    @Bean
    public ConfigurableServletWebServerFactory configurableServletWebServerFactory() {
        //내장톰캣 서버를 설정하고 구성하는 역할을 하는 클래스. 톰캣서버의 설정을 관리함
        TomcatServletWebServerFactory factory=new TomcatServletWebServerFactory() {

            @Override
            protected void postProcessContext(Context ctx) {
                //이 메서드를 오버라이드 하면 jsp속성을 설정하거나 web.xml에서 설정하던 다양한 옵션을 Java Config로 설정할 수 있다
                // 서블릿 컨텍스트를 커스터마이징함
                super.postProcessContext(ctx);

                JspPropertyGroup group=new JspPropertyGroup();
                group.addUrlPattern("*.jsp");
                group.setPageEncoding("UTF-8");
                group.setScriptingInvalid("true");
                group.addIncludePrelude("/WEB-INF/views/inc/top.jspf");
                group.addIncludeCoda("/WEB-INF/views/inc/foot.jspf");
                group.setTrimWhitespace("true");
                group.setDefaultContentType("text/html");

                JspPropertyGroupDescriptorImpl jspPropertyGroupDescriptor = new JspPropertyGroupDescriptorImpl(
                        group);
                ctx.setJspConfigDescriptor(new JspConfigDescriptorImpl(
                        Collections.singletonList(jspPropertyGroupDescriptor), Collections.emptyList()));

            }
        };
        return factory;

    }
}
