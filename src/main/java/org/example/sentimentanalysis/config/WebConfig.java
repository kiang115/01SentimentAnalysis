package org.example.sentimentanalysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${remote.vue.url}")
    private String vueUrl;
    @Value("${sa-token.token-name}")
    private String saTokenName;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins(vueUrl) // Vue 开发服务器
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders(saTokenName, "Content-Type", "Authorization", "X-Requested-With")
                .allowCredentials(false)
                .maxAge(3600);
    }
}

