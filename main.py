from fastapi import FastAPI, BackgroundTasks
from Dto.receive.InferDataRec import InferDataRes
from Dto.send.InferDataSend import InferenceDataResponse
from Common.Exception import BusinessException, SendBody, add_exception_handlers
from Service.InferService.InferenceEngine import InferenceEngine
from Dto.receive.TrainDataRec import TrainDataRes
engine = InferenceEngine()
app = FastAPI(title="Sentiment Analysis API")
add_exception_handlers(app)
@app.post("/predict")
async def predict(request: InferDataRes, background_tasks: BackgroundTasks):
    # 1. 业务逻辑检测
    if not request.inferenceDomainDataList:
        raise BusinessException("请求的领域数据列表不能为空", 500)

    # 2. 异步处理
    # 注意：这里的 background_inference_task 现在是 async 函数
    # FastAPI 的 background_tasks 会正确调度它
    background_tasks.add_task(engine.background_inference_task, request)

    return SendBody.success(message="任务已提交，正在后台进行推理")

@app.post("/train")
async def train(trainDataRes: TrainDataRes, background_tasks: BackgroundTasks):
    # 使用给定配置，异步进行模型训练，得到Lora模型
    return


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)
