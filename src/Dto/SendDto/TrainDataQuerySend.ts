
export interface TrainDataQuerySend {
    pageNum: number; // 当前页码 后端默认为1
    pageSize: number | null; // 表格行数，允许为空让后端走默认值
    content: string; // 筛选内容
    label: number | null; // 筛选标签 0|1，不筛选时为 null
    source: '' | 'original' | 'upload' | 'corrected'; // 筛选来源，不筛选时为空字符串
    domainId: number | null; // 筛选领域，不筛选时为 null
    orderName: '' | 'time' | 'count'; // 排序字段，不排序时为空字符串
    order: '' | 'asc' | 'desc'; // 排序方式，不排序时为空字符串
}
// 前端格式要求
// 输入框 用于输入content
// 表格使用筛选功能 标签对应 0|1  source对应 original|upload|corrected domainId 领域(领域名字使用TrainDataListRec中的domains)
//  数据显示名字，不要显示原始id.（都只能单选）
// 然后是排序方式，希望点击表头的time可以选择时间排序，点击表头的count可以选择训练次数排序，()点击表头其他列不排序
// 都是用原生的elemnetPlus表格实现
// 不懂看[Table 表格 | Element Plus](https://element-plus.org/zh-CN/component/table)这个网站

// 要返回的查询数据结构示例
//{
//   "pageNum": 1,
//   "pageSize": null,
//   "content": "",
//   "label": null,
//   "source": "",
//   "domainId": null,
//   "orderName": "",
//   "order": ""
// }
