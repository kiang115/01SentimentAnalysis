/**
 * SSE 推理任务快照（event: model:inference_event:first / model:inference_event）
 */
export interface InferTaskSnapshot {
  taskId: number
  processedCount: number
  duration: number
  currentTime: string
  status: number
  statusMsg: string
}
