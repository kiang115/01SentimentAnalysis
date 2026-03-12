package org.example.sentimentanalysis.config;

import org.example.sentimentanalysis.dto.redisDto.InferTaskSnapshot;
import org.example.sentimentanalysis.dto.redisDto.TrainTaskSnapshot;
import org.example.sentimentanalysis.enums.TaskStatusEnum;
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
// 配置sse服务模板，运行代码在GenericSseService
@Configuration
public class SseConfiguration {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
//   先写类，再注册！！
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
                return data.getStatus().equals(TaskStatusEnum.SUCCESS.getCode()) || data.getStatus().equals(TaskStatusEnum.FAILED.getCode());
            }
        }, redisTemplate, objectMapper);
    }

    @Bean
    public GenericSseService<TrainTaskSnapshot> trainSseService() {
        return new GenericSseService<>(new SseTaskContext<TrainTaskSnapshot>() {
            @Override
            public String getEventName() {
                return "model:train_event";
            }

            @Override
            public String getChannel() {
                return "model:train_channel";
            }

            @Override
            public String getHashKey() {
                return "model:train_hashkey";
            }

            @Override
            public Class<TrainTaskSnapshot> getDataClass() {
                return TrainTaskSnapshot.class;
            }

            @Override
            public boolean isFinalStatus(TrainTaskSnapshot data) {
                return data.getStatus().equals(TaskStatusEnum.SUCCESS.getCode()) || data.getStatus().equals(TaskStatusEnum.FAILED.getCode());
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
                                                        GenericSseService<InferTaskSnapshot> inferService,
                                                        GenericSseService<TrainTaskSnapshot> trainService
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);

        // 为每个 Service 添加监听
        container.addMessageListener(
                (msg, p) -> inferService.onMessage(new String(msg.getBody())),
                new ChannelTopic(inferService.getChannel())
        );
        container.addMessageListener(
                (msg, p) -> trainService.onMessage(new String(msg.getBody())),
                new ChannelTopic(trainService.getChannel())
        );

        return container;
    }
}
