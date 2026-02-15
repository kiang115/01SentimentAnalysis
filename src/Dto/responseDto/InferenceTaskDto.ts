/** 与后端 modelApi.listInferTasks 返回的 data 项中 modelInfoList 元素结构一致 */
export interface ModelInfo {
  modelId: number
  modelVersion: number
  domainName: string
}

/** 与后端 listInferTasks 返回的 data 列表中单条任务结构一致（可空字段与接口一致） */
export interface InferenceTaskDto {
  taskId: number
  inferenceStartTime: string
  inferenceEndTime: string | null
  inferenceDuration: number | null
  processedCount: number | null
  avgProcessSpeed: number | null
  modelInfoList: ModelInfo[]
  processStatusName: string
}