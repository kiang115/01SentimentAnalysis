/**
 * GET /TrainPanel 返回的训练配置数据
 */
export interface DefaultParaItem {
  paraId: number
  loraR: number
  loraAlpha: number
  epochs: number
  batchSize: number
  learningRate: number
  randomSeed: number
  loraModules: Array<'query' | 'key' | 'value' | 'dense'>
  trainSplitRatio: number
}

export interface TrainDomainItem {
  domainId: number
  domainName: string
  correctedNum: number
  uploadNum: number
  originalNum: number
  defaultParaList: DefaultParaItem[]
}

export interface TrainPanelRec {
  trainParaList: TrainDomainItem[]
}
