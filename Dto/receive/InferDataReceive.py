from pydantic import BaseModel
from typing import List
from decimal import Decimal

class InferenceDomainComment(BaseModel):
    # 评论id
    commentId: int
    # 评论内容
    content: str

class InferenceDomainData(BaseModel):
    # 领域相关
    domainId: int
    domainUrl: str
    # 模型相关
    modelId: int
    modelVersion: str
    # 统计与列表
    inferenceCommentNums: int
    inferenceDomainCommentList: List[InferenceDomainComment]
# 发送过来需要推理的数据
class InferenceRequest(BaseModel):
    taskId: int
    inferenceDomainDataList: List[InferenceDomainData]
