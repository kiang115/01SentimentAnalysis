# fastapi全局配置
FAST_CONFIG = {
    "REDIS_URL":"redis://default:121144@localhost:6379/0",
    "INFERENCE_TASKS_HASH_KEY":"model:inference_hashkey",
    "INFERENCE_TASKS_CHANNEL":"model:inference_channel",

    # 向springboot发送信息的url配置
    "SPRINGBOOT_BASE_URL":"http://127.0.0.1:8888",
    "INFERENCE_RES_URL":"/InferenceResultProcess"
}
