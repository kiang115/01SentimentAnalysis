import json
from dataclasses import dataclass, asdict
from datetime import datetime

#todo  统一status的含义与springboot后台的含义
@dataclass
class InferTaskSnapshot:
    taskId: int
    processedCount: int
    duration: int #单位为秒
    status: int
    statusMsg: str
    currentTime: str = None
    processSpeed: float = 0.0

    def __post_init__(self):
        # 初始化时间并处理浮点数精度
        if not self.currentTime:
            self.currentTime = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    def to_json(self) -> str:
        return json.dumps(asdict(self), ensure_ascii=False)

# 使用方法：
# snapshot = TaskSnapshot(task_id, count, duration, status, msg)
# payload_json = snapshot.to_json()
# snapshot = replace(old_snapshot, currentTime=None)

