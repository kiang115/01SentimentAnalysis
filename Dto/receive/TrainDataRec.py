from pydantic import BaseModel
from typing import List, Optional
from decimal import Decimal


class TrainDataItem(BaseModel):
    # 训练数据id
    id: int
    # 训练文本内容
    content: str
    # 训练标签
    label: int


class TrainDataSourceGroup(BaseModel):
    # 数据来源，例如 corrected/upload/original
    source: str
    # 当前来源下的训练数据列表
    trainDataList: List[TrainDataItem]


class TrainDataRes(BaseModel):
    # 任务和领域信息
    taskId: int
    domainId: int
    domainUrl: str
    #  模型版本
    modelVersion: str
    # 训练数据
    trainDataList: List[TrainDataSourceGroup]
    # LoRA参数
    loraR: int
    loraAlpha: int
    loraModules: List[str]
    # 训练参数
    epochs: int
    batchSize: int
    learningRate: Decimal
    randomSeed: int
    trainSplitRatio: Decimal
    ifOverTrain: bool
    baseModelVersion:Optional[str] = None
    baseModelId:Optional[int] = None

