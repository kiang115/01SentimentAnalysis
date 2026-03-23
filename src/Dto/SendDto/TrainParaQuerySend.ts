
export interface TrainParaQuerySend {
    pageNum: number; // 当前页码 后端默认为1
    pageSize: number | null; // 表格行数，允许为空让后端走默认值
    paraId: string; // 按照参数id筛选，空字符串表示不筛选
    domainId: number | null; // 按照领域id筛选，不筛选时为 null
}
// 第一次查询可以直接设置为全null即可，后面查询需要根据前端筛选条件进行查询
// 前端格式要求
// 表头的领域字段设置筛选，单选
//  参数id表头中 显示一个id输入框，用于筛选id
// 查询示例
//{
//   "pageNum": 1,
//   "pageSize": 8,
//   "paraId": null,
//   "domainId": null
// }
