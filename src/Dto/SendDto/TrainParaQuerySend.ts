
export interface TrainParaQuerySend {
    // 下面的皆可为null 后端已经设置默认值
    pageNum: number;//当前页码 后端默认为1
    pageSize: number;// 默认为空即可 表格行数 后端默认为8
    paraId: string; // 按照参数id筛选 为空不筛选
    domainId: number;// 按照领域id筛选 为空不筛选
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