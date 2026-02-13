import json
from datetime import datetime
import redis.asyncio as redis
from Config.FastapiConfig import CONFIG
from Dto.Redis.InferTaskSnapshot import InferTaskSnapshot
# --- Redis 配置 ---
# decode_responses=True 自动将 Redis 返回的 bytes 转为 str
r = redis.from_url(CONFIG["REDIS_URL"], decode_responses=True)
TASKS_HASH_KEY = CONFIG["INFERENCE_TASKS_HASH_KEY"]  # 存储所有任务最新状态的 Hash
TASKS_CHANNEL = CONFIG["INFERENCE_TASKS_CHANNEL"]  # 发布实时进度的频道


async def update_task_redis(inferTaskSnapshot:InferTaskSnapshot):
    """
    更新 Redis 中的任务快照并发布实时消息
    status: 0-进行中, 1-完成, 2-异常
    """
    currentTime = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    # duration = round(inferTaskSnapshot.duration, 2)
    snapshot = InferTaskSnapshot(inferTaskSnapshot.taskId, inferTaskSnapshot.processedCount, inferTaskSnapshot.duration, inferTaskSnapshot.status, inferTaskSnapshot.statusMsg)
    payload_json = snapshot.to_json()

    # 1. 存入 Hash 结构
    await r.hset(TASKS_HASH_KEY, inferTaskSnapshot.taskId, payload_json)
    # 2. 发布消息
    await r.publish(TASKS_CHANNEL, payload_json)
