package org.example.sentimentanalysis.config;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.EnvironmentVariableCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 示例：在SpringBoot中创建配置Bean
@Configuration
public class OssConfig {
    @Value("${oss.region}")
    private String region;
    @Value("${oss.endpoint}")
    private String endpoint;

    @Bean(destroyMethod = "close")
    public OSSClient ossClient() {
        System.out.println("ID: " + System.getenv("OSS_ACCESS_KEY_ID"));
        System.out.println("Secret: " + System.getenv("OSS_ACCESS_KEY_SECRET"));
        // 使用环境变凭证提供者
        CredentialsProvider provider = new EnvironmentVariableCredentialsProvider();
        return OSSClient.newBuilder()
                .credentialsProvider(provider)
                .region(region)
                .endpoint(endpoint)
                .build();
    }
}
