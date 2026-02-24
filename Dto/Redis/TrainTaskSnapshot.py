import json
from dataclasses import dataclass, asdict
from datetime import datetime


@dataclass
class TrainTaskSnapshot:
    taskId: int
    duration: float
    status: int # 0-待处理, 1-处理中, 2-已完成 3-失败
    statusMsg: str
    currentEpoch: int = 0
    totalEpochs: int = 0
    currentBatch: int = 0
    epochTotalBatches: int = 0
    progressPercent: float = 0.0
    currentTime: str = None
    processSpeed: float = 0.0
    accuracy: float = 0.0
    precisionRate: float = 0.0
    recallRate: float = 0.0
    f1Score: float = 0.0

    def __post_init__(self):
        if not self.currentTime:
            self.currentTime = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        self.duration = round(self.duration, 2)
        self.progressPercent = round(max(0.0, min(100.0, self.progressPercent)), 2)
        self.processSpeed = round(self.processSpeed, 2)
        self.accuracy = round(self.accuracy, 4)
        self.precisionRate = round(self.precisionRate, 4)
        self.recallRate = round(self.recallRate, 4)
        self.f1Score = round(self.f1Score, 4)

    def to_json(self) -> str:
        return json.dumps(asdict(self), ensure_ascii=False)
