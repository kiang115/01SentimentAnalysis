from fastapi import FastAPI, Request
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from Common.SendBody import SendBody
# --- 2. 自定义业务异常 ---
class BusinessException(Exception):
    def __init__(self, message: str, code: int = 500):
        self.message = message
        self.code = code


def _error_response(message: str, code: int = 500):
    body = SendBody.fail(message=message, code=code)
    return JSONResponse(
        status_code=200,
        content=jsonable_encoder(body)
    )

#todo 给这里的异常处理器增加一个返回到前段的装置，可以自定url。这样才能让springboot知道任务状态，否者不会有任务状态产生的
# --- 3. 注册异常处理器 ---
def add_exception_handlers(app: FastAPI):
    # 捕获自定义业务异常 (比如：模型加载失败、输入参数不合法)
    @app.exception_handler(BusinessException)
    async def business_exception_handler(request: Request, exc: BusinessException):
        print(f"系统异常: {str(exc)}")
        return _error_response(exc.message, exc.code)

    # 捕获所有系统异常 (比如：代码报错、未定义的逻辑错误)
    @app.exception_handler(Exception)
    async def global_exception_handler(request: Request, exc: Exception):
        print(f"系统异常: {str(exc)}")
        return _error_response(f"系统异常: {str(exc)}", 500)

# 正确抛出异常的方法
# from Host_Service.Host_Service import BusinessException
# raise BusinessException("模型加载失败", 500)
