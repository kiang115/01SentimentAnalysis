package org.example.sentimentanalysis.service.impl;

import lombok.Data;
import org.example.sentimentanalysis.context.SseTaskContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class GenericSseService<T> {
    private final String eventName;
    private final String channel;
    private final String hashKey;
    private final Class<T> dataClass;
    private final SseTaskContext<T> context;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public GenericSseService(SseTaskContext<T> context,
                             StringRedisTemplate redisTemplate,
                             ObjectMapper objectMapper) {
        this.eventName = context.getEventName();
        this.context = context;
        this.channel = context.getChannel();
        this.hashKey = context.getHashKey();
        this.dataClass = context.getDataClass();
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // 处理 Redis 消息
    public void onMessage(String payload) {
        try {
            T data = objectMapper.readValue(payload, dataClass);
            broadcast(eventName, data);

            if (context.isFinalStatus(data)) {
                checkAndCloseIfAllFinished();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SseEmitter addConnection() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        try {
            // 获取快照
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
            List<T> taskList = entries.values().stream()
                    .map(v -> {
                        try {
                            return objectMapper.readValue((String) v, dataClass);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
//                全量推送
            emitter.send(SseEmitter.event().name(eventName+":first").data(taskList));

            if (taskList.isEmpty() || taskList.stream().allMatch(context::isFinalStatus)) {
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
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
                return false;
            } catch (Exception e) {
                return true;
            }
        });
    }

    private void checkAndCloseIfAllFinished() {
        Map<Object, Object> snapshots = redisTemplate.opsForHash().entries(hashKey);
        boolean anyRunning = snapshots.values().stream()
                .map(v -> {
                    try {
                        return objectMapper.readValue((String) v, dataClass);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .anyMatch(t -> !context.isFinalStatus(t));

        if (!anyRunning) {
            for (SseEmitter emitter : emitters) {
                emitter.complete();
            }
            emitters.clear();
        }
    }
}
//todo 前端重连机制：
//SSE 默认会自动重连，但当你的后端因为 checkAndCloseIfAllFinished() 主动关闭连接后，前端可能会尝试再次连接。
//
//todo 建议：在发送 completed 状态后，前端如果判断所有任务都结束了，可以手动调用 eventSource.close()，避免无谓的重连请求。
//多用户隔离（如果需要）：
//你目前的实现是 广播模式（所有连接的人都能看到所有任务）。如果你的系统未来需要“用户 A 只能看到用户 A 的任务”，你需要在 addConnection 时传入 userId，并在 emitters 存储时使用 Map<String, List<SseEmitter>> 按用户分组。
//
//异常关闭清理：
//现在的 checkAndCloseIfAllFinished 是在监听到“结束状态”时触发的。
//
//思考：如果 Redis 里的数据因为某种原因（比如程序崩溃）没更新到完成态，连接可能会一直挂着。
//todo 建议：可以给 SseEmitter 设置一个合理的超时时间（你现在设置的是 30 分钟，很安全），或者定期清理掉那些长时间没动作的连接。
