/** 与后端 ModelTreeChartSend 一致：模型树状图接口返回结构 */
export interface ModelTreeChartRec {
  domainNameList: string[];
  modelTreeNodeList: ModelTreeNodeRec[];
}

/** 树节点，对应后端 ModelTreeNode；JSON 字段为 name / value */
export interface ModelTreeNodeRec {
  name: string;
  active: boolean;
  value: number | null;
  children?: ModelTreeNodeRec[] | null;
}
