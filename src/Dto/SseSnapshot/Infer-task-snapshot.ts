/**
 * SSE 推理任务快照（event: model:inference_event:first / model:inference_event）
 */
export interface InferTaskSnapshot {
    //  任务id
    taskId: number
    // 当前已经处理评论数 卡片上展示
    processedCount: number
    // 推理持续时间 单位秒
    duration: number
    // 当前时间 eg"2026-02-14 20:20:47", 卡片上展示
    currentTime: string
    // 状态代码 0:处理中 1:成功完成 2:异常完成
    status: number
    // 状态描述，用于在卡片上展示 处理中、成功完成、异常完成
    statusMsg: string
    //   平均处理速度
    processSpeed: number | null
}
