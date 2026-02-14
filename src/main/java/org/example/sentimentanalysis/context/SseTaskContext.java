package org.example.sentimentanalysis.context;

public interface SseTaskContext<T> {
    String getEventName();
    String getChannel();         // Redis Pub/Sub 频道
    String getHashKey();       // Redis Hash 存储的 Key
    Class<T> getDataClass();   // 实体类的类型
    boolean isFinalStatus(T data); // 判断任务是否结束的逻辑
}