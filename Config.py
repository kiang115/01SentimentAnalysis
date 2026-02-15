# config.py
import os
from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache


class Settings(BaseSettings):
    # BERT 相关配置
    bert_model_path: str = ""
    # lora模型的根目录路径
    lora_url: str = ""
    dropout: float = 0.0
    num_classes: int = 0
    parallel_workers: int = 0
    max_length: int = 0
    max_gpu_batch_size: int = 0

    # Redis 相关配置
    # redis的连接url
    redis_url: str = ""
    # 推理任务存入redis的key名字
    inference_tasks_hash_key: str = ""
    # 推理任务用redis publish的频道名字
    inference_tasks_channel: str = ""

    # SpringBoot 相关配置
    # 后端的基础url
    springboot_base_url: str = ""
    # 推理结果返回给springboot的url
    inference_res_url: str = ""

    # 根据 ENV 环境变量加载对应的 .env 文件，默认 development
    model_config = SettingsConfigDict(
        # env_file=f".env.{os.getenv('ENV', 'development')}",
        # TODO 大坑，这里的.路径可不是config.py的相对路径，而是main.py的相对路径
        env_file=".env.development",
        env_file_encoding="utf-8",
        extra="ignore"
    )


# 启动命令
# ENV=development uvicorn main:app --reload
# 在需要使用的地方 from Config import get_settings, Settings # 引入函数和类
# get_settings().app_name
@lru_cache
def get_settings():
    return Settings()
