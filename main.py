from fastapi import FastAPI, BackgroundTasks
from Dto.receive.InferDataReceive import InferenceRequest
from Dto.send.InferDataSend import InferenceDataResponse
from Common.Exception import BusinessException, SendBody
from Service.InferService.InferenceEngine import InferenceEngine
engine = InferenceEngine()
app = FastAPI(title="Sentiment Analysis API")

@app.post("/predict")
async def predict(request: InferenceRequest, background_tasks: BackgroundTasks):
    # 1. 业务逻辑检测
    if not request.inferenceDomainDataList:
        raise BusinessException("请求的领域数据列表不能为空", 500)

    # 2. 异步处理
    # 注意：这里的 background_inference_task 现在是 async 函数
    # FastAPI 的 background_tasks 会正确调度它
    background_tasks.add_task(engine.background_inference_task, request)

    # 3. 立即返回
    data_content = InferenceDataResponse(
        taskId=request.taskId,
        processStatus=1,  # 这里的 1 是你业务定义的接收状态
        results=[]
    )

    return SendBody.success(data=data_content, message="任务已提交，正在后台进行推理")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)
