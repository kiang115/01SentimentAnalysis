/** 与后端返回的推理+训练次数月度热力图数据结构一致 */
export interface TasksHotChart {
  // 热力图，当前年份
  year: string;
  // 热力图，当前月份
  month: string;
  // 热力图，推理任务次数的最大值，用于设置热力图颜色
  maxTotalCount: number;
  // 按天列表
  dailyDataList: DailyData[];
}

/** 单日热力图数据结构，对应后端 DailyData 内部类 */
export interface DailyData {
  // 本月每天日期，格式为yyyy-MM-dd。如果当天没有训练，下面的count通通为0
  date: string;
  // 推理次数
  inferCount: number;
  // 训练次数
  trainCount: number;
  // inferCount+trainCount
  totalCount: number;
}
//  这是一个月度热力图，当鼠标悬浮应该显示 总任务次数+推理任务次数+训练任务次数
//  热力图颜色根据总任务次数进行设置
//  图名字为 X月 任务执行热力图
//数据示例 data对应TasksHotChart
// 后台已经保证数据的格式，列表的对应关系和排序一致性。vue无需额外判断，排序，解析或者异常处理！！！
//{
//   "code": 200,
//   "data": {
//     "year": "2026",
//     "month": "2",
//     "maxTotalCount": 11,
//     "dailyDataList": [
//       {
//         "date": "2026-02-01",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-02",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-03",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-04",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-05",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-06",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-07",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-08",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-09",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-10",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-11",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-12",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-13",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-14",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-15",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-16",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-17",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-18",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-19",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-20",
//         "inferCount": 2,
//         "trainCount": 9,
//         "totalCount": 11
//       },
//       {
//         "date": "2026-02-21",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-22",
//         "inferCount": 1,
//         "trainCount": 3,
//         "totalCount": 4
//       },
//       {
//         "date": "2026-02-23",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-24",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-25",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-26",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-27",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       },
//       {
//         "date": "2026-02-28",
//         "inferCount": 0,
//         "trainCount": 0,
//         "totalCount": 0
//       }
//     ]
//   },
//   "message": "操作成功"
// }