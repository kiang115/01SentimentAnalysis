from dataclasses import replace
import redis.asyncio as redis
from Dto.Redis.InferTaskSnapshot import InferTaskSnapshot
from Dto.Redis.TrainTaskSnapshot import TrainTaskSnapshot
from Config import get_settings

# --- Redis 配置 ---

r = redis.from_url(get_settings().redis_url, decode_responses=True)


async def update_infer_task(inferTaskSnapshot: InferTaskSnapshot, currentTime=None):
    """
    更新 Redis 中的任务快照并发布实时消息
    status: 0-待处理 1处理中 2成功 3失败
    """
    snapshot = replace(inferTaskSnapshot, currentTime=currentTime)
    try:
        snapshot.processSpeed = round(snapshot.processedCount / snapshot.duration, 2)
    except ZeroDivisionError:
        snapshot.processSpeed = 0.0
    payload_json = snapshot.to_json()

    # 1. 存入 Hash 结构，直接使用 get_settings()
    await r.hset(get_settings().inference_tasks_hash_key, inferTaskSnapshot.taskId, payload_json)
    # 2. 发布消息，直接使用 get_settings()
    await r.publish(get_settings().inference_tasks_channel, payload_json)


async def update_train_task(trainTaskSnapshot: TrainTaskSnapshot, currentTime=None):
    """
    更新 Redis 中的训练任务快照并发布实时消息
        status: 0-待处理 1处理中 2成功 3失败
    """
    snapshot = replace(trainTaskSnapshot, currentTime=currentTime)
    try:
        # 以已完成的 batch 数量估算训练速度(batch/s)
        if snapshot.epochTotalBatches > 0 and snapshot.currentEpoch > 0:
            processed_batches = (
                    (snapshot.currentEpoch - 1) * snapshot.epochTotalBatches + snapshot.currentBatch
            )
        else:
            processed_batches = snapshot.currentBatch
        snapshot.processSpeed = round(processed_batches / snapshot.duration, 2)
    except ZeroDivisionError:
        snapshot.processSpeed = 0.0

    payload_json = snapshot.to_json()
    print(payload_json)

    # 1. 存入 Hash 结构
    await r.hset(get_settings().train_tasks_hash_key, trainTaskSnapshot.taskId, payload_json)
    # 2. 发布消息
    await r.publish(get_settings().train_tasks_channel, payload_json)
