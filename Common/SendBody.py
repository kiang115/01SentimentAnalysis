from pydantic import BaseModel
from typing import List, Optional, Any, TypeVar, Generic

# 定义泛型变量
T = TypeVar("T")


class SendBody(BaseModel, Generic[T]):
    code: int
    data: Optional[T] = None
    message: str

    @classmethod
    def success(cls, data: T = None, message: str = "操作成功", code: int = 200):
        # 返回类实例而非字典
        return cls(code=code, data=data, message=message)

    @classmethod
    def fail(cls, message: str = "操作错误", code: int = 500, data: Any = None):
        return cls(code=code, data=data, message=message)
