# fastapi全局配置
CONFIG = {
    "REDIS_URL":"redis://default:121144@localhost:6379/0",

    "INFERENCE_TASKS_HASH_KEY":"model:inference_tasks",
    "INFERENCE_TASKS_CHANNEL":"model:inference_events"

}
