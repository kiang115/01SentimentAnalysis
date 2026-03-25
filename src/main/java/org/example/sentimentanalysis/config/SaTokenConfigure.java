package org.example.sentimentanalysis.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 潘多码(微信 : panduoma888)
 * @version 1.0.0
 * @description Sa-Token配置类
 * @website www.panduoma.com
 * @copyright 公众号: 潘多码
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // 注册 Sa-Token 拦截器，打开注解式鉴权功能
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/**")
                .notMatch("/login")
                .notMatch("/register")
                .notMatch("/file/upload")
                .notMatch("/error")
                .notMatch("/doc.html")
                .notMatch("/swagger-ui/**")
                .notMatch("/swagger-resources/**")
                .notMatch("/webjars/**")
                .notMatch("/v3/api-docs/**")
                .check(r -> {
                    if (HttpMethod.OPTIONS.matches(SaHolder.getRequest().getMethod())) {
                        return;
                    }
                    StpUtil.checkLogin();
                }))).addPathPatterns("/**");
    }
}
