import json
import asyncio
import time
from datetime import datetime
import redis.asyncio as redis
from fastapi import FastAPI, BackgroundTasks
from Dto.request.InferenceRequest import InferenceRequest
from Dto.response.InferenceDataResponse import InferenceDataResponse
from Inference_Engine import InferenceEngine
from HostService.HostService import BusinessException, ResultBody

app = FastAPI(title="Sentiment Analysis API")
engine = InferenceEngine()

# --- Redis 配置 ---
# decode_responses=True 自动将 Redis 返回的 bytes 转为 str
r = redis.from_url("redis://default:121144@localhost:6379/0", decode_responses=True)
TASKS_HASH_KEY = "model:inference_tasks"  # 存储所有任务最新状态的 Hash
TASKS_CHANNEL = "model:inference_events"  # 发布实时进度的频道


# --- 工具函数：更新 Redis 状态 ---
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


# --- 后台任务逻辑 ---
# main.py

async def background_inference_task(request: InferenceRequest):
    all_results = []
    processed_count = 0
    start_time = time.time()
    task_id = request.taskId
    loop = asyncio.get_running_loop()  # 获取当前异步事件循环

    # --- 定义回调函数 ---
    def on_batch_done(batch_size_count: int):
        nonlocal processed_count
        processed_count += batch_size_count
        current_duration = time.time() - start_time

        # 跨线程触发异步 Redis 更新
        # 使用 create_task 因为 update_task_redis 是 async 函数
        loop.call_soon_threadsafe(
            lambda: asyncio.create_task(
                update_task_redis(task_id, processed_count, current_duration, 0, "processing")
            )
        )

    try:
        # 0. 初始状态
        await update_task_redis(task_id, 0, 0, 0, "processing")

        for domain_data in request.inferenceDomainDataList:
            # 1. 传入回调函数 on_batch_done
            res = await asyncio.to_thread(
                engine.predict_domain_batch,
                domain_url=domain_data.domainUrl,
                version=str(domain_data.modelVersion),
                comments_data=[c.model_dump() for c in domain_data.inferenceDomainCommentList],
                on_batch_complete=on_batch_done  # <--- 关键：把回调传进去
            )
            all_results.extend(res)
        # 2. 正常完成
        await update_task_redis(task_id, processed_count, time.time() - start_time, 1, "completed")

    except Exception as e:
        # 3. 异常处理
        await update_task_redis(task_id, processed_count, time.time() - start_time, 2, f"Error: {str(e)}")


# --- 业务逻辑接口 ---
@app.post("/predict")
async def predict(request: InferenceRequest, background_tasks: BackgroundTasks):
    # 1. 业务逻辑检测
    if not request.inferenceDomainDataList:
        raise BusinessException("请求的领域数据列表不能为空", 500)

    # 2. 异步处理
    # 注意：这里的 background_inference_task 现在是 async 函数
    # FastAPI 的 background_tasks 会正确调度它
    background_tasks.add_task(background_inference_task, request)

    # 3. 立即返回
    data_content = InferenceDataResponse(
        taskId=request.taskId,
        processStatus=1,  # 这里的 1 是你业务定义的接收状态
        results=[]
    )

    return ResultBody.success(data=data_content, message="任务已提交，正在后台进行推理")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)
