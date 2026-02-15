from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Any, Optional

from pydantic import BaseModel
from typing import List, Optional, Any, TypeVar, Generic

# 定义泛型变量
T = TypeVar("T")


class ResultBody(BaseModel, Generic[T]):
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


# --- 2. 自定义业务异常 ---
class BusinessException(Exception):
    def __init__(self, message: str, code: int = 500):
        self.message = message
        self.code = code


# --- 3. 注册异常处理器 ---
def add_exception_handlers(app: FastAPI):
    # 捕获自定义业务异常 (比如：模型加载失败、输入参数不合法)
    @app.exception_handler(BusinessException)
    async def business_exception_handler(request: Request, exc: BusinessException):
        return JSONResponse(
            status_code=200,  # 给 SpringBoot 返回 200，由 SpringBoot 判断内部 code
            content=ResultBody.fail(message=exc.message, code=exc.code)
        )

    # 捕获所有系统异常 (比如：代码报错、未定义的逻辑错误)
    @app.exception_handler(Exception)
    async def global_exception_handler(request: Request, exc: Exception):
        return JSONResponse(
            status_code=200,
            content=ResultBody.fail(message=f"系统异常: {str(exc)}", code=500)
        )

# 正确抛出异常的方法
# from Host_Service.Host_Service import BusinessException
# raise BusinessException("模型加载失败", 500)
