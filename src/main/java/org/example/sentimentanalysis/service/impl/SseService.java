package org.example.sentimentanalysis.service.impl;

import jakarta.annotation.PostConstruct;
import org.example.sentimentanalysis.dto.redisDto.InferTaskSnapshot;
import org.example.sentimentanalysis.exception.CustomBusinessException;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.example.sentimentanalysis.enums.RedisInferenceTaskStatusEnum.COMPLETED;
import static org.example.sentimentanalysis.enums.RedisInferenceTaskStatusEnum.ERRORTASK;

@Service
public class SseService {
    private static final String TASKS_HASH_KEY = "tasks_status_hash";
    private static final String TASKS_CHANNEL = "tasks_channel";

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 这里使用 Spring 管理监听器，更稳健
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic(TASKS_CHANNEL));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter() {
        // 构造一个全局唯一的监听器，并设置收到消息时的动作
        return new MessageListenerAdapter((MessageListener) (message, pattern) -> {
            try {
                String payload = new String(message.getBody());
                InferTaskSnapshot task = objectMapper.readValue(payload, InferTaskSnapshot.class);

                System.out.print("Received message: " + task);
                // 1. 广播更新
                broadcast("InferTaskUpdate", task);

                // 2. 如果当前任务是完成/失败状态，检查是否所有任务都结束了
                if (isFinalStatus(task.getStatus())) {
                    checkAndCloseIfAllFinished();
                }
            } catch (Exception e) {
//                #TODO 整个日志系统
                e.printStackTrace(); // 实际生产环境建议记录日志
            }
        });
    }

    public SseEmitter addConnection() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        try {
            // 发送快照
            Map<Object, Object> snapshots = redisTemplate.opsForHash().entries(TASKS_HASH_KEY);
            List<InferTaskSnapshot> taskList = snapshots.values().stream()
                    .map(v -> {
                        try {
                            return objectMapper.readValue((String) v, InferTaskSnapshot.class);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            emitter.send(SseEmitter.event().name("snapshot").data(taskList));

            // 如果此时 Redis 里本来就没任务，或者任务全都是完成态，直接关闭
            if (taskList.isEmpty() || taskList.stream().allMatch(t -> isFinalStatus(t.getStatus()))) {
                emitter.complete();
                return emitter;
            }

        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        emitters.add(emitter);
        return emitter;
    }

    private void broadcast(String eventName, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

    /**
     * 核心逻辑：检查 Redis 中是否还有正在运行的任务
     */
    private void checkAndCloseIfAllFinished() {
//        1. 获取redis中推理任务的的全部数据 taskId->String
        Map<Object, Object> snapshots = redisTemplate.opsForHash().entries(TASKS_HASH_KEY);
        boolean anyRunning = snapshots.values().stream()
                .map(v -> {
                    try {
//                        将redis中String转化为java对象
                        return objectMapper.readValue((String) v, InferTaskSnapshot.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .anyMatch(t -> !isFinalStatus(t.getStatus()));

        // 如果全部完成，关闭所有连接
        if (!anyRunning) {
            for (SseEmitter emitter : emitters) {
                emitter.complete();
            }
            emitters.clear();
        }
    }

    private boolean isFinalStatus(Integer status) {
        return status.equals(COMPLETED.getCode()) || status.equals(ERRORTASK.getCode());
    }
}
