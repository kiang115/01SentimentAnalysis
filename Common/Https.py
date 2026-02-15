import httpx
from fastapi.encoders import jsonable_encoder

from Common.SendBody import SendBody

async def post_springboot(payload: SendBody, url: str):
    async with httpx.AsyncClient() as client:
        # clean_data = jsonable_encoder(payload)
        # json只能接收字典类型
        print(f"DEBUG - Sending to SpringBoot: {jsonable_encoder(payload)}")
        try:
            payload
            response = await client.post(url, json=jsonable_encoder(payload), timeout=30.0)
            response.raise_for_status()
            print(f"Callback success: {response.status_code}")
        except Exception as e:
            print(f"Callback failed: {e}")
