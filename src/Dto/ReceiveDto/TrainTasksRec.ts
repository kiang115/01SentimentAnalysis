/**
 * 训练任务项
 */
export interface TrainTasksItem {
  /** 训练任务id */
  id: number
  /** 发起人id */
  creatorId: number
  /** 领域id */
  domainId: number
  //  领域名字
  domainName: string
  // 从审核中人工修正的数据来源
  correctedNum: number
  // 从自己上传的评论的数据来源
  uploadNum: number
  // 原始数据来源
  originalNum: number
  /** 训练状态 (0:待处理, 1:处理中, 2:成功完成, 3:失败完成) */
  status: number
  /** 状态对应的实际文案/错误信息 */
  statusMsg: string
  /** 训练开始时间 */
  startTime: string
  /** 训练结束时间 */
  endTime: string | null
  /** 训练最终持续时间(秒) 整数秒，可为空 */
  duration: number | null
  /** 训练总评论数 */
  totalData: number
  /** LORA_R */
  loraR: number
  /** lora_alpha */
  loraAlpha: number
  /** 训练轮数 */
  epochs: number
  /** 训练批次大小 */
  batchSize: number
  /** 学习率 */
  learningRate: number
  /** 随机种子 */
  randomSeed: number
  /** Lora模块列表, eg: ["query", "key", "value", "dense"] */
  loraModules: string[]
  /** 训练集比例 */
  trainSplitRatio: number
  /** 是否重新训练，是=1 否=0 */
  ifOverTrain: number
  /** 训练得到的模型id */
  modelId: number | null
  // 训练得到的模型版本
  modelVersion: string | null
  /** 总体准确率(%) */
  accuracy: number | null
  /** 验证集精确率 */
  precisionRate: number | null
  /** 验证集召回率 */
  recallRate: number | null
  /** 验证集f1分数 */
  f1Score: number | null
  /** 训练集loss变化趋势数组 */
  trainLossList: number[] | null
  /** 训练集准确率变化趋势数组 */
  trainAccList: number[] | null
  /** 验证集loss变化趋势数组 */
  valLossList: number[] | null
  /** 验证集准确率变化趋势数组 */
  valAccList: number[] | null
  /** 记录创建时间 */
  createTime: string
  /** 记录更新时间 */
  updateTime: string
}

/**
 * 训练任务返回结构
 */
export interface TrainTasksRec {
  /** 是否全部完成 (1:是, 0:否) */
  ifAllFinished: number
  /** 训练任务列表 */
  trainTasksList: TrainTasksItem[]
}
