import httpx
from fastapi.encoders import jsonable_encoder

from Service.Host_Service.HostService import ResultBody


async def notify_springboot(payload: ResultBody, url: str):
    async with httpx.AsyncClient() as client:
        # clean_data = jsonable_encoder(payload)
        # json只能接收字典类型
        try:
            response = await client.post(url, json=jsonable_encoder(payload), timeout=30.0)
            response.raise_for_status()
            print(f"Callback success: {response.status_code}")
        except Exception as e:
            print(f"Callback failed: {e}")
