from fastapi import FastAPI, BackgroundTasks
from Dto.request.InferenceRequest import InferenceRequest
from Dto.response.InferenceDataResponse import InferenceDataResponse
from Inference_Engine import InferenceEngine
from HostService.HostService import BusinessException, ResultBody
import time

app = FastAPI(title="Sentiment Analysis API")
engine = InferenceEngine()


# --- 后台任务逻辑 ---
# 将耗时的推理逻辑抽离到一个独立函数中
def background_inference_task(request: InferenceRequest):
    """
    实际执行推理的函数，在后台运行。
    注意：因为是异步，结果通常需要写入数据库或日志。
    """
    all_results = []
    startTime = time.time()
    try:
        for domain_data in request.inferenceDomainDataList:
            res = engine.predict_domain_batch(
                domain_url=domain_data.domainUrl,
                version=str(domain_data.modelVersion),
                comments_data=[c.model_dump() for c in domain_data.inferenceDomainCommentList]
            )
            all_results.extend(res)
            endTime = time.time()
        # TODO: 这里建议将 all_results 存入数据库或 Redis，
        # 因为 HTTP 响应已经早早返回给前端了。
        print(f"Task {request.taskId} 推理完成，结果数: {len(all_results)},，耗时{endTime-startTime}")

    except Exception as e:
        # 后台任务报错无法抛给前端，只能打日志或记录到数据库状态
        print(f"后台推理发生异常: {str(e)}")


# --- 5. 业务逻辑 ---
@app.post("/predict")
# --- 业务逻辑 ---
async def predict(request: InferenceRequest, background_tasks: BackgroundTasks):
    # 1. 【数据初始检测】
    # 场景 A: Pydantic 会自动根据你定义的类型做基础检测（类型不对会直接报 422）

    # 场景 B: 手动业务逻辑检测
    if not request.inferenceDomainDataList:
        # 直接抛出你定义好的异常，会自动触发 global_exception_handler
        raise BusinessException("请求的领域数据列表不能为空", 500)

    # 2. 【异步处理】
    # 使用 background_tasks.add_task 将耗时任务丢到后台执行
    # 代码会立即往下走，不会阻塞在这里
    background_tasks.add_task(background_inference_task, request)

    # 3. 【立即返回】
    # 注意：此时 all_results 还没产生，所以 results 传空列表或 None
    data_content = InferenceDataResponse(
        taskId=request.taskId,
        processStatus=1,  # 1 代表已接收或执行中
        results=[]  # 异步模式下，当前请求拿不到结果
    )

    # 返回统一包装后的结果
    return ResultBody.success(data=data_content, message="任务已提交，正在后台进行推理")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)
