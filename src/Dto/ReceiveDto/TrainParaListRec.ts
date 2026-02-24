import type {PageInfo} from '@/Dto/ReceiveDto/PageInfo';

export interface TrainParaItem {
    paraId: number;//数据编号
    domainId: number;//领域id
    domainName: string;//领域名称
    loraR: number; //R参数
    loraAlpha: number; //alpha参数
    epochs: number; //训练轮数
    batchSize: number; // 批次大小
    learningRate: number;// 学习率
    randomSeed: string; // 随机种子
    loraModules: string[]; // lora模块
    trainSplitRatio: number;// 训练集比例
}

/**
 * 领域/分类的具体结构
 */
export interface DomainItem {
    domainId: number; //领域编号 不展示
    domainName: string;//领域名称 需要用于筛选时的下拉框展示
}

/**
 * Data 字段的具体结构
 */
export interface TrainParaListRec {
    pageInfo: PageInfo<TrainParaItem>;//这个是分页+数据列表
    domains: DomainItem[]; //这个是用于领域选择下拉框的
}
//参考数据
//{
//   "code": 200,
//   "data": {
//     "pageInfo": {
//       "endRow": 6,
//       "hasNextPage": false,
//       "hasPreviousPage": false,
//       "isFirstPage": true,
//       "isLastPage": true,
//       "list": [
//         {
//           "paraId": 1,
//           "domainId": 1,
//           "loraR": 8,
//           "loraAlpha": 16,
//           "epochs": 5,
//           "batchSize": 64,
//           "learningRate": 0.00001,
//           "randomSeed": 42,
//           "loraModules": [
//             "query",
//             "key",
//             "value",
//             "dense"
//           ],
//           "trainSplitRatio": null,
//           "domainName": "外卖"
//         },
//         {
//           "paraId": 2,
//           "domainId": 1,
//           "loraR": 16,
//           "loraAlpha": 32,
//           "epochs": 10,
//           "batchSize": 32,
//           "learningRate": 0.00002,
//           "randomSeed": 123,
//           "loraModules": [
//             "query",
//             "value"
//           ],
//           "trainSplitRatio": null,
//           "domainName": "外卖"
//         },
//         {
//           "paraId": 3,
//           "domainId": 2,
//           "loraR": 8,
//           "loraAlpha": 16,
//           "epochs": 5,
//           "batchSize": 64,
//           "learningRate": 0.00001,
//           "randomSeed": 42,
//           "loraModules": [
//             "query",
//             "key",
//             "value",
//             "dense"
//           ],
//           "trainSplitRatio": null,
//           "domainName": "电商"
//         },
//         {
//           "paraId": 4,
//           "domainId": 2,
//           "loraR": 32,
//           "loraAlpha": 64,
//           "epochs": 8,
//           "batchSize": 128,
//           "learningRate": 0.000005,
//           "randomSeed": 456,
//           "loraModules": [
//             "query",
//             "key",
//             "value",
//             "dense"
//           ],
//           "trainSplitRatio": null,
//           "domainName": "电商"
//         },
//         {
//           "paraId": 5,
//           "domainId": 3,
//           "loraR": 8,
//           "loraAlpha": 16,
//           "epochs": 5,
//           "batchSize": 64,
//           "learningRate": 0.00001,
//           "randomSeed": 42,
//           "loraModules": [
//             "query",
//             "key",
//             "value",
//             "dense"
//           ],
//           "trainSplitRatio": null,
//           "domainName": "酒店"
//         },
//         {
//           "paraId": 6,
//           "domainId": 3,
//           "loraR": 4,
//           "loraAlpha": 8,
//           "epochs": 15,
//           "batchSize": 16,
//           "learningRate": 0.00003,
//           "randomSeed": 789,
//           "loraModules": [
//             "query",
//             "key"
//           ],
//           "trainSplitRatio": null,
//           "domainName": "酒店"
//         }
//       ],
//       "navigateFirstPage": 1,
//       "navigateLastPage": 1,
//       "navigatePages": 8,
//       "navigatepageNums": [
//         1
//       ],
//       "nextPage": 0,
//       "pageNum": 1,
//       "pageSize": 8,
//       "pages": 1,
//       "prePage": 0,
//       "size": 6,
//       "startRow": 1,
//       "total": 6
//     },
//     "domains": [
//       {
//         "domainId": 1,
//         "domainName": "外卖"
//       },
//       {
//         "domainId": 2,
//         "domainName": "电商"
//       },
//       {
//         "domainId": 3,
//         "domainName": "酒店"
//       }
//     ]
//   },
//   "message": "操作成功"
// }