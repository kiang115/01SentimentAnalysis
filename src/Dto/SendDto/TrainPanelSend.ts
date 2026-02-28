/**
 * POST /TrainDataCheck 发送给后端的训练参数配置
 */
export interface TrainPanelSend {
  domainId: number
  correctedNum: number
  uploadNum: number
  originalNum: number
  loraR: number
  loraAlpha: number
  epochs: number
  batchSize: number
  learningRate: number
  randomSeed: number
  loraModules: Array<'query' | 'key' | 'value' | 'dense'>
  trainSplitRatio: number
  isOverTrain: boolean
  baseModelId: number | null
}
