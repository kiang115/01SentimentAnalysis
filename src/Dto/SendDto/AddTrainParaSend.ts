export interface AddTrainParaSend {
    // 以下数据均为必填项
    domainId: number;//领域id
    loraR: number; //R参数
    loraAlpha: number; //alpha参数
    epochs: number; //训练轮数
    batchSize: number; // 批次大小
    learningRate: number;// 学习率
    randomSeed: string; // 随机种子
    loraModules: string[]; // lora模块
    trainSplitRatio: number;// 训练集比例
}
// 