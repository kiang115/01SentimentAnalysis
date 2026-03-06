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

inferEngine = InferenceEngine()
train_engine = TrainEngine()


def  get_gpu_load():
    # 因为已经在 lifespan 初始化过了，这里直接使用即可
    handle = pynvml.nvmlDeviceGetHandleByIndex(0)
    info = pynvml.nvmlDeviceGetMemoryInfo(handle)

    # 计算百分比：(已用 / 总量) * 100
    # round(x, 2) 表示保留两位小数
    usage_percent = round((info.used / info.total) * 100, 2)

    return usage_percent  # 直接返回数字，例如 45.23

async def handle_infer_message(message: AbstractIncomingMessage) -> None:
    # 这是一个上下文管理器。它的作用是：
    # 自动确认 (Ack)：如果 with 块内的代码成功运行，它会自动向 RabbitMQ 发送确认信号（Ack），告诉服务器消息已处理。
    # 异常处理 (Nack)：如果代码运行报错，它会发送否认信号（Nack），根据配置，消息可能会重新回到队列或进入死信队列。
    # 1. 解析重试次数 (x-death)
    death_headers = message.headers.get("x-death", [])
    # 找到当前队列的失败计数
    retry_count = 0
    if death_headers:
        # 拿到最近一次死信的信息
        retry_count = death_headers[0].get("count", 0)

    try:
        # 2. 负载准入检查
        mem_free = get_gpu_load()
        if mem_free < 80:
             # 如果是负载高，requeue=True 直接放回原队列头部，不增加 x-death 计数
             await message.reject(requeue=True)
             await asyncio.sleep(5)
             return


        # 3. 执行任务
        async with message.process(requeue=False):
            payload = json.loads(message.body.decode("utf-8"))
            # 将 JSON 反序列化成你的请求 DTO
            infer_req = InferDataRes(**payload)
            # 这里直接调用你原来在 background task 里做的事情
            await inferEngine.background_inference_task(infer_req)

    except RuntimeError as e:
        print(f"发生 OOM，已失败 {retry_count} 次，触发延迟重试...")
        # 关键：nack 且 requeue=False，消息会根据配置进入延迟队列
        await message.reject(requeue=False)


async def handle_train_message(message: AbstractIncomingMessage) -> None:
    async with message.process(requeue=False):
        payload = json.loads(message.body.decode("utf-8"))
        # 将 JSON 反序列化成你的请求 DTO
        train_req = TrainDataRes(**payload)
        await train_engine.background_train_task(train_req)


async def setup_rabbitmq():
    """负责建立连接并启动消费者的逻辑"""
    connection = await aio_pika.connect_robust(RABBITMQ_URL)
    channel = await connection.channel()
    # 设置消费者只能接受两个任务
    await channel.set_qos(prefetch_count=2)

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
    try:
        pynvml.nvmlInit()
        print("NVML initialized successfully.")
    except Exception as e:
        print(f"Failed to initialize NVML: {e}")
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
