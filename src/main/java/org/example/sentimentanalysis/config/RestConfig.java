package org.example.sentimentanalysis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {
    @Bean
    public RestTemplate restTemplate() {
        // 可以在这里配置连接超时、读取超时等
        return new RestTemplate();
    }
}
