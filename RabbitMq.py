import asyncio
import json
from contextlib import asynccontextmanager
from typing import Any

import aio_pika
from aio_pika.abc import AbstractIncomingMessage
from fastapi import FastAPI

from Dto.receive.InferDataRec import InferDataRes
from Dto.receive.TrainDataRec import TrainDataRes
from Service.InferService.InferenceEngine import InferenceEngine
from Service.TrainService.TrainEngine import TrainEngine

RABBITMQ_URL = "amqp://guest:guest@localhost/"

engine = InferenceEngine()
train_engine = TrainEngine()

async def handle_infer_message(message: AbstractIncomingMessage) -> None:
    # 这是一个上下文管理器。它的作用是：
    # 自动确认 (Ack)：如果 with 块内的代码成功运行，它会自动向 RabbitMQ 发送确认信号（Ack），告诉服务器消息已处理。
    # 异常处理 (Nack)：如果代码运行报错，它会发送否认信号（Nack），根据配置，消息可能会重新回到队列或进入死信队列。
    async with message.process():
        payload = json.loads(message.body.decode("utf-8"))
        # 将 JSON 反序列化成你的请求 DTO
        infer_req = InferDataRes(**payload)
        # 这里直接调用你原来在 background task 里做的事情
        await engine.background_inference_task(infer_req)

async def handle_train_message(message: AbstractIncomingMessage) -> None:
    async with message.process():
        payload = json.loads(message.body.decode("utf-8"))
        # 将 JSON 反序列化成你的请求 DTO
        train_req = TrainDataRes(**payload)
        await train_engine.background_train_task(train_req)


async def setup_rabbitmq():
    """负责建立连接并启动消费者的逻辑"""
    connection = await aio_pika.connect_robust(RABBITMQ_URL)
    channel = await connection.channel()

    # 声明队列
    infer_queue = await channel.declare_queue("sentiment.infer.req", durable=True)
    train_queue = await channel.declare_queue("sentiment.train.req", durable=True)

    # 启动消费者
    await infer_queue.consume(handle_infer_message)
    await train_queue.consume(handle_train_message)

    print("RabbitMQ consumers started.")
    return connection  # 返回连接对象用于后续关闭

@asynccontextmanager
async def lifespan(app: FastAPI):
    # --- 【Startup 阶段】：应用启动时执行 ---
    # 启动 RabbitMQ 连接
    rabbitmq_conn = await setup_rabbitmq()

    # 你甚至可以把资源存入 app.state 方便在其他路由中使用
    app.state.rabbitmq_conn = rabbitmq_conn

    yield  # 这里是应用运行的分界点

    # --- 【Shutdown 阶段】：应用关闭时执行 ---
    print("Shutting down RabbitMQ connection...")
    await rabbitmq_conn.close()
    print("Cleanup complete.")