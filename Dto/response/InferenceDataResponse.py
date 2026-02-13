from pydantic import BaseModel
from typing import List

class CommentResult(BaseModel):
    commentId: int
    modelSentiment: int  # 0或1
    positiveProb: float
    negativeProb: float
    confidence: float

class InferenceDataResponse(BaseModel):
    taskId: int
    processStatus: int   # 1:正常, 2:错误
    results: List[CommentResult]
