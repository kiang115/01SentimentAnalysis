package org.example.sentimentanalysis.config;

import org.example.sentimentanalysis.dto.redisDto.InferTaskSnapshot;
import org.example.sentimentanalysis.service.impl.GenericSseService;
import org.example.sentimentanalysis.context.SseTaskContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import tools.jackson.databind.ObjectMapper;

import static org.example.sentimentanalysis.enums.RedisInferenceTaskStatusEnum.COMPLETED;
import static org.example.sentimentanalysis.enums.RedisInferenceTaskStatusEnum.ERRORTASK;

@Configuration
public class SseConfiguration {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    // --- 任务 1：推理任务配置 ---
    @Bean
    public GenericSseService<InferTaskSnapshot> inferSseService() {
        return new GenericSseService<>(new SseTaskContext<InferTaskSnapshot>() {
            @Override
            public String getEventName() {
                return "model:inference_event";
            }

            @Override
            public String getChannel() {
                return "model:inference_channel";
            }

            @Override
            public String getHashKey() {
                return "model:inference_hashkey";
            }

            @Override
            public Class<InferTaskSnapshot> getDataClass() {
                return InferTaskSnapshot.class;
            }

            @Override
            public boolean isFinalStatus(InferTaskSnapshot data) {
                return data.getStatus().equals(COMPLETED.getCode()) || data.getStatus().equals(ERRORTASK.getCode());
            }
        }, redisTemplate, objectMapper);
    }
//        @Bean
//    public GenericSseService<FileTaskDto> fileSseService() {
//        return new GenericSseService<>(new SseTaskContext<FileTaskDto>() {
//            @Override
//            public String getTopic() { return "file_updates"; }
//            @Override
//            public String getHashKey() { return "file_status_hash"; }
//            @Override
//            public Class<FileTaskDto> getDataClass() { return FileTaskDto.class; }
//            @Override
//            public boolean isFinalStatus(FileTaskDto data) {
//                return "SUCCESS".equals(data.getState()) || "FAILED".equals(data.getState());
//            }
//        }, redisTemplate, objectMapper);
//    }

    // --- 统一注册 Redis 监听器 ---
    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory factory,
                                                        GenericSseService<InferTaskSnapshot> inferService
//                                                        GenericSseService<FileTaskDto> fileService
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);

        // 为每个 Service 添加监听
        container.addMessageListener(
                (msg, p) -> inferService.onMessage(new String(msg.getBody())),
                new ChannelTopic(inferService.getChannel())
        );
//        container.addMessageListener(
//            (msg, p) -> fileService.onMessage(new String(msg.getBody())),
//            new ChannelTopic("file_updates")
//        );
        return container;
    }
}
