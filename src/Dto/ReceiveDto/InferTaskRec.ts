/** 与后端 modelApi.listInferTasks 返回的 data 项中 modelInfoList 元素结构一致 */
export interface ModelInfo {
  modelId: number
  modelVersion: string
  domainName: string
}

/** 与后端 listInferTasks 返回的 data.inferenceTasksList 中单条任务结构一致（可空字段与接口一致） */
export interface InferTaskDetail {
  taskId: number
  inferenceStartTime: string
  inferenceEndTime: string | null
  /** 推理持续时间，单位秒（整数） */
  inferenceDuration: number | null
  processedCount: number | null
  avgProcessSpeed: number | null
  modelInfoList: ModelInfo[]
  /** 状态码：0 待处理，1 处理中，2 成功，3 失败 */
  status: number
  /** 状态描述，用于展示；status=3 时为错误详情 */
  statusMsg: string
}

/** 与后端 listInferTasks 返回的 data 结构一致 */
export interface InferTaskRec {
  /** 推理任务列表 */
  inferenceTasksList: InferTaskDetail[]
  /** 是否全部完成：1=是，0=否 */
  ifAllFinished: number
}
