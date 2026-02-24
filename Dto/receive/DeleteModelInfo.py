from pydantic import BaseModel
class DeleteModelInfo(BaseModel):
    domainUrl: str
    modelVersion: str