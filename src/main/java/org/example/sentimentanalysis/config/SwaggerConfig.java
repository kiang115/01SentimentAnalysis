package org.example.sentimentanalysis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("评论情感分析系统")
                        .version("1.0")
                        .description("评论情感分析系统接口文档")
                        .contact(new Contact().name("kiang").url("www.kiang.com")));
    }
}
