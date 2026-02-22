/** 与后端返回的饼图推理数据结构一致 */
export interface InferPieChart {
  // [已推理+未推理]用于下拉框
  pieNameList: string[];
  // 领域名字，用于饼状图的扇区名字
  domainNameList: string[];
  // 已经完成推理的评论
  inferredPie: PieData[];
  // 未完成推理的评论
  unInferredPie: PieData[];
}

/** 饼状图数据项结构，对应后端 PieData 类 */
export interface PieData {
  // 领域名字
  domainName: string;
  // 对应评论数量
  commentNum: number;
}
//饼状图需要展示各个领域评论数量的占比
// 两个饼状图，通过一个下拉框切换，下拉框和饼状图名字写好了，就是pieNameList中的名字
//示例数据 data对应InferPieChart
// 后台已经保证数据的格式，列表的对应关系和排序一致性 vue无需额外判断，排序，解析或者异常处理！！！
//{
//   "code": 200,
//   "data": {
//     "pieNameList": [
//       "已完成推理评论分布",
//       "未完成推理评论分布"
//     ],
//     "domainNameList": [
//       "外卖",
//       "电商",
//       "酒店"
//     ],
//     "inferredPie": [
//       {
//         "domainName": "外卖",
//         "commentNum": 794
//       },
//       {
//         "domainName": "电商",
//         "commentNum": 0
//       },
//       {
//         "domainName": "酒店",
//         "commentNum": 0
//       }
//     ],
//     "unInferredPie": [
//       {
//         "domainName": "外卖",
//         "commentNum": 413
//       },
//       {
//         "domainName": "电商",
//         "commentNum": 3
//       },
//       {
//         "domainName": "酒店",
//         "commentNum": 3
//       }
//     ]
//   },
//   "message": "操作成功"
// }