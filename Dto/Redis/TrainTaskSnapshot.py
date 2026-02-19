import json
from dataclasses import dataclass, asdict
from datetime import datetime
@dataclass
class TrainTaskSnapshot:
    taskId: int
