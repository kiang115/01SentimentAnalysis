import type {PageInfo} from '@/Dto/ReceiveDto/PageInfo';

export interface ModelInfoItem {
        modelId: number;//模型编号
        domainId: number;//领域id
        domainName: string;//领域名称
        modelVersion: string;//模型版本
        sourceName: string;//模型来源
        inferredNum: number;//推理评论总数量
        correctedNum: number;//人工修正数量
        accuracy: number;//模型总体准确率
        description: string;//模型描述
        createdAt: string;//创建时间
        baseModelVersion:string;//基础模型版本
}

/**
 * 领域/分类的具体结构
 */
export interface DomainItem {
    domainId: number; //领域编号 不展示
    domainName: string;//领域名称 需要用于筛选领域时的下拉框展示
}

/**
 * Data 字段的具体结构
 */
export interface ModelsListRec {
    pageInfo: PageInfo<ModelInfoItem>;//这个是分页+数据列表
    domains: DomainItem[]; //这个是用于领域选择下拉框的
}

// 前端样式
//前端样式要求 ModelInfoItem列表展示为一个表格，除了domainId以外，其他字段均按照字段顺序 内容展示 注释内容就为表头名
// 下面的json数据即对应ModelsListRec结构示例

/**
{
  "code": 200,
  "data": {
    "pageInfo": {
      "endRow": 2,
      "hasNextPage": false,
      "hasPreviousPage": false,
      "isFirstPage": true,
      "isLastPage": true,
      "list": [
        {
          "modelId": 1,
          "domainId": 1,
          "domainName": "外卖",
          "modelVersion": "0.10",
          "sourceName": "手动上传",
          "inferredNum": 100,
          "correctedNum": 20,
          "accuracy": 80,
          "description": "这是第一版外卖模型",
          "createdAt": "2026-02-24T10:45:05"
        },
        {
          "modelId": 2,
          "domainId": 1,
          "domainName": "外卖",
          "modelVersion": "0.10",
          "sourceName": "手动上传",
          "inferredNum": 100,
          "correctedNum": 20,
          "accuracy": 80,
          "description": "这是第一版外卖模型",
          "createdAt": "2026-02-24T10:45:05"
        }
      ],
      "navigateFirstPage": 1,
      "navigateLastPage": 1,
      "navigatePages": 8,
      "navigatepageNums": [
        1
      ],
      "nextPage": 0,
      "pageNum": 1,
      "pageSize": 8,
      "pages": 1,
      "prePage": 0,
      "size": 2,
      "startRow": 1,
      "total": 2
    },
    "domains": [
      {
        "domainId": 1,
        "domainName": "外卖"
      },
      {
        "domainId": 2,
        "domainName": "电商"
      },
      {
        "domainId": 3,
        "domainName": "酒店"
      }
    ]
  },
  "message": "操作成功"
}
 */
