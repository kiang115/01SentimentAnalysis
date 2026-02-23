export interface TrainDataAddSend{
    content: string;
    label: number;
    domainId: number;
}
// 前端样式，点击弹出一个面板，
// 第一行 输入数据内容 (输入框 检测非空)
//第二行 选择数据标签 (单选框 好评 或者 差评 (好评label为1 差评为0))
// 第三方 一个下拉框 选择领域 (下拉框 显示领域名字 但是返回领域id 数据可从TrainDataListRec的DomainItem中得到)