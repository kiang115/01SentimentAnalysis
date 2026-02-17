/**
 * GET /InferencePanel 返回的面板配置数据
 */
export interface DomainModelItem {
  modelId: number
  modelVersion: string
}

export interface InferenceConfigItem {
  domainId: number
  domainName: string
  uninferencedCommentNums: number
  domainModelsDataList: DomainModelItem[]
}

export interface InferPanelRec {
  inferenceConfigDataList: InferenceConfigItem[]
}
