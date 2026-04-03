import base64
import os
import shutil
from typing import List

from fastapi import FastAPI, BackgroundTasks, HTTPException, Form, UploadFile, File, Body
from pydantic import json
from starlette.responses import FileResponse

from Config import get_settings

from Dto.receive.ModelInfo import ModelInfo
from Dto.receive.InferDataRec import InferDataRes
from Dto.send.InferDataSend import InferenceDataResponse
from Common.Exception import BusinessException, SendBody, add_exception_handlers
from Service.InferService.InferenceEngine import InferenceEngine
from Service.TrainService.TrainEngine import TrainEngine
from Dto.receive.TrainDataRec import TrainDataRes
from Config import get_settings, Settings

engine = InferenceEngine()
train_engine = TrainEngine()
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
    # 校验数据外层列表不为空+内层列表任意一个都不空
    if not trainDataRes.trainDataList or not any(g.trainDataList for g in trainDataRes.trainDataList):
        raise BusinessException("请求的训练数据列表不能为空", 500)
    background_tasks.add_task(train_engine.background_train_task, trainDataRes)
    return SendBody.success(message="训练任务已提交，正在后台执行")


@app.post("/deleteModels")
async def deleteModels(modelInfoList: List[ModelInfo]):
    settings = get_settings()
    success = 0
    failed = 0
    for info in modelInfoList:
        lora_path = os.path.join(settings.lora_url, info.domainUrl, f"v-{info.modelVersion}")
        if os.path.exists(lora_path):
            shutil.rmtree(lora_path)
            success += 1
        else:
            failed += 1
    return SendBody.success(message=f"成功删除{success}条模型,失败{failed}条")


@app.post("/downLoadModel")  # 保持 Post 接收 ModelInfo
async def download(modelInfo: ModelInfo, background_tasks: BackgroundTasks):
    settings = get_settings()
    lora_path = os.path.join(settings.lora_url, modelInfo.domainUrl, f"v-{modelInfo.modelVersion}")

    if not os.path.exists(lora_path):
        raise HTTPException(status_code=404, detail="Lora folder not found")

    lora_name = f"{modelInfo.domainUrl}_v-{modelInfo.modelVersion}"
    zip_file_base = os.path.join(settings.lora_url, "temp", lora_name)
    zip_file_path = f"{zip_file_base}.zip"

    # 创建压缩包
    shutil.make_archive(zip_file_base, 'zip', lora_path)

    # 注册任务：发送完后删除临时文件
    background_tasks.add_task(os.remove, zip_file_path)

    # 直接返回文件响应
    return FileResponse(
        path=zip_file_path,
        filename=f"{lora_name}.zip",
        media_type='application/octet-stream'
    )


@app.post("/uploadModel")
async def upload_model(
        files: List[UploadFile] = File(...),
        domainUrl: str = Form(...),  # 使用 Form 接收平铺的字段
        modelVersion: str = Form(...)  # 使用 Form 接收平铺的字段
):

    settings = get_settings()
    if not domainUrl or not modelVersion:
        raise HTTPException(status_code=400, detail="ModelInfo 缺失字段")

    # 2. 确定存储路径
    # 建议路径中加入 model_version 防止版本覆盖
    lora_path = os.path.join(settings.lora_url, domainUrl, f"v-{modelVersion}")

    # 3. 递归创建文件夹（如果不存在）
    os.makedirs(lora_path, exist_ok=True)

    # 4. 循环保存文件
    for file in files:
        # file.filename 会自动获取 Java 端传来的名字 (如 adapter_model.bin)
        file_dest = os.path.join(lora_path, file.filename)

        # 写入文件
        with open(file_dest, "wb") as f:
            content = await file.read()
            f.write(content)

    return SendBody.success(message=f"模型已保存至 {lora_path}")


@app.post("/uploadGoldTest")
async def upload_gold_test(
        file: UploadFile = File(...),
        domainUrl: str = Form(...)
):
    base = os.path.abspath(os.path.dirname(__file__))
    gold_dir = os.path.join(base, "GoldTest")
    os.makedirs(gold_dir, exist_ok=True)
    dest = os.path.join(gold_dir, f"{domainUrl}.csv")
    with open(dest, "wb") as f:
        content = await file.read()
        f.write(content)
    return SendBody.success(message=f"测试集已保存至 {dest}")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)
