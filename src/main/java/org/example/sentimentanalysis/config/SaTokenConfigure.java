package org.example.sentimentanalysis.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
//        registry.addInterceptor(new SaInterceptor(handle -> {
//                    if (isPreflightRequest()) {
//                        return;
//                    }
//                    StpUtil.checkLogin();
//                }))
//                .addPathPatterns("/**")
//                .excludePathPatterns(
//                        "/login",
//                        "/register",
//                        "/file/upload",
//                        "/download-model/**",
//                        "/error",
//                        "/doc.html",
//                        "/swagger-ui/**",
//                        "/InferenceResultProcess",
//                        "/TrainResultProcess",
//                        "/webjars/**",
//                        "/v3/api-docs/**",
//                        "/api/sse/**"
//                );
    }

    private boolean isPreflightRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false;
        }
        return HttpMethod.OPTIONS.matches(attributes.getRequest().getMethod());
    }
}
