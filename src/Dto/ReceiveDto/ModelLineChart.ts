/** 与后端返回的训练折线图数据结构一致 */
export interface ModelLineChart {
  // 领域名字列表
  domainNameList: string[];
  // 各领域的折线图数据列表
  domainLinesDataList: DomainLinesData[];
}

/** 单领域的折线图数据结构，对应后端 DomainLinesData 内部类 */
export interface DomainLinesData {
  // 领域名称
  domainName: string;
  // 折线图的横坐标 即所有大版本的小版本号集合 X.0 X.1...X.10 X.11...
  allSmallVersions: string[];
  // 多条折线区分 大版本号
  allMajorVersions: string[];
  // 多条折线的数据
  majorVersionDataList: MajorVersionData[];
}

/** 单一大版本的折线数据结构，对应后端 MajorVersionData 内部类 */
export interface MajorVersionData {
  // 大版本号名字
  majorVersion: string;
  // 对应的小版本号的准确率 没有的用null表示
  versionAccuracyList: (number | null)[];
}
// 这是一个折线图
// 首先通过下拉框切换domainNameList中的领域名字
// 折线图显示多条线，每条线对应allMajorVersions中一个大版本号，名字原封不动
// 折线图的x轴为allSmallVersions(名字不变，已保证严格排序)，y轴为versionAccuracyList
// 某个x准确度为null的跳过,直接连接到下一个x

//  data对应ModelLineChart
//  数据已经能保证domainNameList与domainName，allSmallVersions与versionAccuracyList以及其他列表之间的对应关系，vue无需额外判断，排序，解析或者异常处理！！！
//{
//   "code": 200,
//   "data": {
//     "domainNameList": [
//       "外卖",
//       "电商",
//       "酒店"
//     ],
//     "domainLinesDataList": [
//       {
//         "domainName": "外卖",
//         "allSmallVersions": [
//           "X.0",
//           "X.1",
//           "X.2"
//         ],
//         "allMajorVersions": [
//           "0.X",
//           "1.X",
//           "2.X"
//         ],
//         "majorVersionDataList": [
//           {
//             "majorVersion": "0.X",
//             "versionAccuracyList": [
//               0,
//               null,
//               null
//             ]
//           },
//           {
//             "majorVersion": "1.X",
//             "versionAccuracyList": [
//               100,
//               0,
//               null
//             ]
//           },
//           {
//             "majorVersion": "2.X",
//             "versionAccuracyList": [
//               0,
//               0,
//               0
//             ]
//           }
//         ]
//       },
//       {
//         "domainName": "电商",
//         "allSmallVersions": [
//           "X.0"
//         ],
//         "allMajorVersions": [
//           "0.X"
//         ],
//         "majorVersionDataList": [
//           {
//             "majorVersion": "0.X",
//             "versionAccuracyList": [
//               null
//             ]
//           }
//         ]
//       },
//       {
//         "domainName": "酒店",
//         "allSmallVersions": [
//           "X.0"
//         ],
//         "allMajorVersions": [
//           "0.X"
//         ],
//         "majorVersionDataList": [
//           {
//             "majorVersion": "0.X",
//             "versionAccuracyList": [
//               null
//             ]
//           }
//         ]
//       }
//     ]
//   },
//   "message": "操作成功"
// }
// 