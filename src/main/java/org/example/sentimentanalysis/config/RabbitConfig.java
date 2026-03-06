package org.example.sentimentanalysis.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // 交换机
    public static final String EXCHANGE_SENTIMENT = "sentiment.direct";
    public static final String RETRY_EXCHANGE = "sentiment.retry";
    // 队列名
    public static final String QUEUE_INFER_REQ = "sentiment.infer.req";
    public static final String QUEUE_TRAIN_REQ = "sentiment.train.req";
    public static final String QUEUE_RETRY_INFER_REQ = "infer.retry.queue";
    public static final String QUEUE_RETRY_TRAIN_REQ = "train.retry.queue";

    // 任务标签 路由键
    public static final String ROUTING_INFER_REQ = "sentiment.infer.req";
    public static final String ROUTING_TRAIN_REQ = "sentiment.train.req";
    public static final String ROUTING_RETRY_INFER_REQ = "sentiment.retry.infer.req";
    public static final String ROUTING_RETRY_TRAIN_REQ = "sentiment.retry.train.req";

    // --- 交换机定义 ---
    @Bean
    public DirectExchange sentimentExchange() {
        return new DirectExchange(EXCHANGE_SENTIMENT, true, false);
    }

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE);
    }

    // 定义业务队列
    @Bean
    public Queue inferReqQueue() {
//        -----队列名 死信交换机 如果成为死信，对应的死信路由键
        return QueueBuilder.durable(QUEUE_INFER_REQ)
                .deadLetterExchange(RETRY_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_RETRY_INFER_REQ) // 消息死亡后转发的路由键
                .build();
    }

    @Bean
    public Queue trainReqQueue() {
        return QueueBuilder.durable(QUEUE_TRAIN_REQ)
                .deadLetterExchange(RETRY_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_RETRY_TRAIN_REQ)
                .build();
    }

    @Bean
    public Queue retryInferQueue() {
//        这里已经是死信队列了，等过了30s又变为了死信的死信，我们把他配置为原队列，就得到了延迟的效果。
        return QueueBuilder.durable(QUEUE_RETRY_INFER_REQ)
                .ttl(30000)
                .deadLetterExchange(EXCHANGE_SENTIMENT)
                .deadLetterRoutingKey(ROUTING_INFER_REQ)
                .build();
    }

    @Bean
    public Queue retryTrainQueue() {
                return QueueBuilder.durable(QUEUE_RETRY_TRAIN_REQ)
                .ttl(30000)
                .deadLetterExchange(EXCHANGE_SENTIMENT)
                .deadLetterRoutingKey(ROUTING_TRAIN_REQ)
                .build();
    }


    // 业务绑定
    @Bean
    public Binding inferReqBinding(Queue inferReqQueue, DirectExchange sentimentExchange) {
//        (将任务键值与队列与交换机绑定)
        return BindingBuilder.bind(inferReqQueue).to(sentimentExchange).with(ROUTING_INFER_REQ);
    }

    @Bean
    public Binding trainReqBinding(Queue trainReqQueue, DirectExchange sentimentExchange) {
        return BindingBuilder.bind(trainReqQueue).to(sentimentExchange).with(ROUTING_TRAIN_REQ);
    }

    // 死信绑定
    @Bean
    public Binding deadInferReqBinding(Queue retryInferQueue, DirectExchange retryExchange) {
        return BindingBuilder
                .bind(retryInferQueue) // 绑定死信队列
                .to(retryExchange)
                .with(ROUTING_RETRY_INFER_REQ); // 对应 deadLetterRoutingKey
    }

    @Bean
    public Binding deadTrainReqBinding(Queue retryTrainQueue, DirectExchange retryExchange) {
        return BindingBuilder
                .bind(retryTrainQueue) // 绑定死信队列
                .to(retryExchange)
                .with(ROUTING_RETRY_TRAIN_REQ); // 对应 deadLetterRoutingKey
    }


}

