export interface TrainTaskSnapshot {
  /** 任务唯一标识 ID */
  taskId: number;
  /** 任务已执行时长（秒） */
  duration: number;
  /** 任务状态码（0:待处理, 1:运行中, 2:完成, 3:失败） */
  status: number;
  /** 任务状态描述信息 */
  statusMsg: string;
  /** 当前执行的轮次（epoch） */
  currentEpoch: number;
  /** 总训练轮次 */
  totalEpochs: number;
  /** 当前执行的批次（batch） */
  currentBatch: number;
  /** 单轮次总批次数量 */
  epochTotalBatches: number;
  /** 任务进度百分比（0.0 ~ 100.0） */
  progressPercent: number;
  /** 当前时间戳（格式：YYYY-MM-DD HH:mm:ss） */
  currentTime: string;
  /** 处理速度（可根据业务定义单位，如 batch/秒） */
  processSpeed: number;
  /** 模型准确率 */
  accuracy: number;
  /** 精确率 */
  precisionRate: number;
  /** 召回率 */
  recallRate: number;
  /** F1 分数（综合精确率和召回率的指标） */
  f1Score: number;
}