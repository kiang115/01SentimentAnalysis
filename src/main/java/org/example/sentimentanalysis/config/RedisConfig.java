package org.example.sentimentanalysis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {
    @Bean
//    返回一个用来监听redis消息的容器 connectionFactory相当于一个连接的配置器
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

//    @Bean
////    将redis中存储的字节流变为字符串
//    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
//        return new StringRedisTemplate(factory);
//    }
}

