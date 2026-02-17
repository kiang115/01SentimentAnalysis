/** 与后端 modelApi.listInferTasks 返回的 data 项中 modelInfoList 元素结构一致 */
export interface ModelInfo {
  modelId: number
  modelVersion: number
  domainName: string
}

/** 与后端 listInferTasks 返回的 data 列表中单条任务结构一致（可空字段与接口一致） */
export interface InferTaskDetail {
  // 任务id
  taskId: number
  // 推理开始时间
  inferenceStartTime: string
  // 推理结束时间
  inferenceEndTime: string | null
  // 推理持续时间 单位是毫秒
  inferenceDuration: number | null
  // 当前已经处理评论数
  processedCount: number | null
  // 平均处理速度
  avgProcessSpeed: number | null
  // 模型信息列表
  modelInfoList: ModelInfo[]
  // 状态描述
  processStatusName: string
}