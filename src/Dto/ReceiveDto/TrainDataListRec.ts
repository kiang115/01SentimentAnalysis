import type {PageInfo} from '@/Dto/ReceiveDto/PageInfo';

export interface DataItem {
    id: number;//数据编号
    content: string;//数据内容
    domainId: number; //领域id 表格不显示
    domainName: string; //领域名称
    label: number; //数据标签
    source: 'original' | 'upload' | 'corrected' | string; // 数据集来源
    trainCount: number;// 训练次数
    createdAt: string; // 创建日期
}

/**
 * 领域/分类的具体结构
 */
export interface DomainItem {
    domainId: number; //领域编号 不展示
    domainName: string;//领域名称 需要用于筛选时的下拉框展示
}

/**
 * Data 字段的具体结构
 */
export interface TrainDataListRec {
    pageInfo: PageInfo<DataItem>;//这个是分页+数据列表
    domains: DomainItem[]; //这个是用于领域选择下拉框的
}
//前端样式要求 DataItem列表展示为一个表格，除了domainId以外，其他字段均按照字段顺序 内容展示 注释内容就为表头名
// 返回数据格式为 data对应的类容
// {
//   "code": 200,
//   "data": {
//     "pageInfo": {
//       "endRow": 7,
//       "hasNextPage": false,
//       "hasPreviousPage": false,
//       "isFirstPage": true,
//       "isLastPage": true,
//       "list": [
//         {
//           "id": 1,
//           "content": "外卖配送非常快，骑手态度也很好，饭菜还是热乎的，给五星好评！",
//           "domainId": 1,
//           "domainName": "外卖",
//           "label": 1,
//           "source": "original",
//           "trainCount": 0,
//           "createdAt": "2026-02-19T14:31:58"
//         },
//         {
//           "id": 2,
//           "content": "送餐超时了半个小时，面条都泡烂了，体验非常糟糕。",
//           "domainId": 1,
//           "domainName": "外卖",
//           "label": 0,
//           "source": "upload",
//           "trainCount": 0,
//           "createdAt": "2026-02-19T14:31:58"
//         },
//         {
//           "id": 3,
//           "content": "商家漏发了一份例汤，联系客服处理很快，补发了红包。",
//           "domainId": 1,
//           "domainName": "外卖",
//           "label": 1,
//           "source": "corrected",
//           "trainCount": 0,
//           "createdAt": "2026-02-19T14:31:58"
//         },
//         {
//           "id": 4,
//           "content": "这件衣服的质量非常好，颜色和图片描述一致，没有色差。",
//           "domainId": 2,
//           "domainName": "电商",
//           "label": 1,
//           "source": "original",
//           "trainCount": 0,
//           "createdAt": "2026-02-19T14:31:58"
//         },
//         {
//           "id": 5,
//           "content": "快递包装破损严重，里面的电子产品外壳有划痕，申请退货。",
//           "domainId": 2,
//           "domainName": "电商",
//           "label": 0,
//           "source": "upload",
//           "trainCount": 0,
//           "createdAt": "2026-02-19T14:31:58"
//         },
//         {
//           "id": 6,
//           "content": "物流速度一般，但是客服回复问题的态度很专业，解决了我的疑惑。",
//           "domainId": 2,
//           "domainName": "电商",
//           "label": 1,
//           "source": "original",
//           "trainCount": 0,
//           "createdAt": "2026-02-19T14:31:58"
//         },
//         {
//           "id": 7,
//           "content": "酒店地理位置优越，出门就是地铁站，房间采光很好，卫生很干净。",
//           "domainId": 3,
//           "domainName": "酒店",
//           "label": 1,
//           "source": "original",
//           "trainCount": 0,
//           "createdAt": "2026-02-19T14:31:58"
//         },
//         {
//           "id": 8,
//           "content": "隔音效果太差了，半夜能听到走廊的人说话，完全没法休息。",
//           "domainId": 3,
//           "domainName": "酒店",
//           "label": 0,
//           "source": "corrected",
//           "trainCount": 0,
//           "createdAt": "2026-02-19T14:31:58"
//         }
//       ],
//       "navigateFirstPage": 1,
//       "navigateLastPage": 1,
//       "navigatePages": 8,
//       "navigatepageNums": [
//         1
//       ],
//       "nextPage": 0,
//       "pageNum": 1,
//       "pageSize": 8,
//       "pages": 1,
//       "prePage": 0,
//       "size": 8,
//       "startRow": 0,
//       "total": 8
//     },
//     "domains": [
//       {
//         "domainId": 1,
//         "domainName": "外卖"
//       },
//       {
//         "domainId": 2,
//         "domainName": "电商"
//       },
//       {
//         "domainId": 3,
//         "domainName": "酒店"
//       }
//     ]
//   },
//   "message": "操作成功"
// }

