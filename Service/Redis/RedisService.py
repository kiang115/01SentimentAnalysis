from dataclasses import replace
import redis.asyncio as redis
from Dto.Redis.InferTaskSnapshot import InferTaskSnapshot
from Config import get_settings

# --- Redis 配置 ---

r = redis.from_url(get_settings().redis_url, decode_responses=True)


async def update_infer_task(inferTaskSnapshot: InferTaskSnapshot, currentTime=None):
    """
    更新 Redis 中的任务快照并发布实时消息
    status: 0-进行中, 1-完成, 2-异常
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
