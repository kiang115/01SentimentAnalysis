from pydantic import BaseModel
from typing import List

from pydantic import BaseModel, Field
#  推理的结果，需要返回给springboot 全部设上默认值，防止springboot解析出错，导致业务不能正常进行
class CommentResult(BaseModel):
    commentId: int = 0
    modelSentiment: int = 0  # 0或1
    positiveProb: float = 0.0
    negativeProb: float = 0.0
    confidence: float = 0.0
    modelId: int = 0
    domainId: int = 0


class InferenceDataResponse(BaseModel):
    taskId: int = 0
    processStatus: int = 3 # 0待处理 1处理中 2完成 3失败
    processCount: int = 0
    taskEndTime: str = ""  # 默认为空字符串而非 None
    taskDuration: int = 0  # 默认 0
    results: List[CommentResult] = Field(default_factory=list)  # 默认为空列表 []
