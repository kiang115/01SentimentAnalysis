/**
 * POST /InferenceDataCheck 请求体
 */

export interface InferenceDomainParaItem {
  domainId: number
  modelId: number
  inferenceReviewNums: number
}

export interface CheckInferDataPara {
  inferenceDomainPara: InferenceDomainParaItem[]
  sort: 'newest' | 'lastest'
}
