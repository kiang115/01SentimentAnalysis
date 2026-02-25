export interface ModelPieChart {
  statusNameList: string[];
  statusCountList: number[];
  domainNameList: string[];
  domainAccuracyList: number[];
  inferredNumList: number[];
  rightNumList: number[];
}
// 示例数据
// {
//   "code": 200,
//   "data": {
//     "statusNameList": [
//       "待推理",
//       "推理中",
//       "已推理",
//       "审核中",
//       "已修正",
//       "已拒绝"
//     ],
//     "statusCountList": [
//       500,
//       152,
//       561,
//       0,
//       0,
//       0
//     ],
//     "domainNameList": [
//       "外卖",
//       "电商",
//       "酒店"
//     ],
//     "domainAccuracyList": [
//       100,
//       0,
//       0
//     ],
//     "inferredNumList": [
//       25,
//       0,
//       0
//     ],
//     "rightNumList": [
//       25,
//       0,
//       0
//     ]
//   },
//   "message": "操作成功"
// }