from fastapi import FastAPI
from Dto.request.InferenceRequest import InferenceRequest
from Dto.response.InferenceDataResponse import InferenceDataResponse
from Inference_Engine import InferenceEngine
from HostService.HostService import BusinessException,ResultBody
app = FastAPI(title="Sentiment Analysis API")
engine = InferenceEngine()

# --- 5. 业务逻辑 ---
@app.post("/predict")
async def predict(request: InferenceRequest):
    # 注意：这里不再需要写 try...except，报错会自动跳到上面的 global_exception_handler
    all_results = []

    # 遍历请求中的所有领域数据
    for domain_data in request.inferenceDomainDataList:
        # 执行推理
        res = engine.predict_domain_batch(
            domain_url=domain_data.domainUrl,
            version=str(domain_data.modelVersion),
            comments_data=[c.model_dump() for c in domain_data.inferenceDomainCommentList]
        )
        all_results.extend(res)

    # 构造业务数据主体
    data_content = InferenceDataResponse(
        taskId=request.taskId,
        processStatus=1,  # 成功
        results=all_results,
        message="Success"
    )

    # 返回统一包装后的结果
    return ResultBody.success(data=data_content)

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)
