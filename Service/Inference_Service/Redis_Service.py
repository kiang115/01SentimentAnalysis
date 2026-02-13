import json
from datetime import datetime
import redis.asyncio as redis
from Config.FastapiConfig import CONFIG

# --- Redis 配置 ---
# decode_responses=True 自动将 Redis 返回的 bytes 转为 str
r = redis.from_url(CONFIG["REDIS_URL"], decode_responses=True)
TASKS_HASH_KEY = CONFIG["INFERENCE_TASKS_HASH_KEY"]  # 存储所有任务最新状态的 Hash
TASKS_CHANNEL = CONFIG["INFERENCE_TASKS_CHANNEL"]  # 发布实时进度的频道


async def update_task_redis(task_id: int, count: int, duration: float, status: int, msg: str):
    """
    更新 Redis 中的任务快照并发布实时消息
    status: 0-进行中, 1-完成, 2-异常
    """
    payload_dict = {
        "taskId": task_id,
        "processedCount": count,
        "duration": round(duration, 2),
        "currentTime": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "status": status,
        "statusMsg": msg
    }
    payload_json = json.dumps(payload_dict, ensure_ascii=False)

    # 1. 存入 Hash 结构 (用于后期查询或页面刷新加载)
    await r.hset(TASKS_HASH_KEY, task_id, payload_json)
    # 2. 发布消息 (用于前端订阅实时更新进度条)
    await r.publish(TASKS_CHANNEL, payload_json)
