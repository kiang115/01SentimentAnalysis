from pydantic import BaseModel
from typing import List
#  推理的结果，需要返回给springboot
class CommentResult(BaseModel):
    commentId: int
    modelSentiment: int  # 0或1
    positiveProb: float
    negativeProb: float
    confidence: float

class InferenceDataResponse(BaseModel):
    taskId: int
    processStatus: int # 0-进行中, 1-完成, 2-异常
    results: List[CommentResult]
