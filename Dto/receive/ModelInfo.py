from pydantic import BaseModel
class ModelInfo(BaseModel):
    domainUrl: str
    modelVersion: str