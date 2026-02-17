/**
 * POST /InferenceDataCheck 发送给后端的推理参数配置
 */

export interface InferenceDomainParaItem {
  domainId: number
  modelId: number
  inferenceReviewNums: number
}

export interface InferPanelSend {
  inferenceDomainPara: InferenceDomainParaItem[]
  sort: 'newest' | 'lastest'
}
