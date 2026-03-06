from fastapi import Request


@app.middleware("http")
async def extract_satoken(request: Request, call_next):
    # 从 header 中提取，注意 Key 的大小写（通常不敏感）
    token = request.headers.get("satoken")

    # 将 token 存入 request.state 方便后续路由使用
    request.state.token = token

    response = await call_next(request)
    return response