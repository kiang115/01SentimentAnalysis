from pydantic import BaseModel
from typing import List

from pydantic import BaseModel, Field
#  推理的结果，需要返回给springboot 全部设上默认值，防止springboot解析出错，导致业务不能正常进行
class TrainDataSend(BaseModel):
    taskId: int
    domainId: int
    modelVersion:str
    processStatus: int  # 0-进行中, 1-完成, 2-异常
    endTime: str
    duration: float
    results: List[int] # 训练数据的id列表
    accuracy: float
    precisionRate: float
    recallRate: float
    f1Score: float
    trainLossList: List[float]
    trainAccList: List[float]
    valLossList: List[float]
    valAccList: List[float]
    epochTotalBatches: int = 0

