export interface ModelsQuerySend {
    pageNum: number; // 当前页码 后端默认为1
    pageSize: number | null; // 表格行数，允许为空让后端走默认值
    modelId: string; // 按照模型id筛选，空字符串表示不筛选
    domainId: number | null; // 按照领域id筛选，不筛选时为 null
    source: 'train' | 'upload' | null; // 模型来源，不筛选时为 null
}
// 第一次查询可以直接设置为全null即可，后面查询需要根据前端筛选条件进行查询
// 前端格式要求
// 表头的领域字段设置筛选，单选
// 表头的来源字段设置筛选，单选 文本显示->发送数据为 训练模型->train  手动上传->upload
//  参数id表头中 显示一个id输入框，用于筛选id
// 查询示例
//{
//   "pageNum": 1,
//   "pageSize": 8,
//   "modelId": null,
//   "domainId": null
//   "source": null
// }
