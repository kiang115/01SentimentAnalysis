/**
 * 商家列表查询参数
 */
export interface MerchantsQuerySend {
    /**
     * 页码 (默认值: 1)
     */
    pageNum?: number;

    /**
     * 每页数量 (默认值: 9)
     */
    pageSize?: number;

    /**
     * 领域id 为空表示不筛选领域
     */
    domainId?: number | null;

    /**
     * 排序方式
     * rating -> 综合得分
     * positive -> 好评率
     * commentNum -> 好评人数
     * 其他或者为空时 按照默认的综合得分排序
     * (排序均为降序排序)
     */
    orderName?: 'rating' | 'positive' | 'commentNum'| null;

    /**
     * 关键字筛选
     * 需要同时筛选 商家名-商品信息
     * 筛选商品名字需要将商家表和商品表join查询，最后仍然只返回包含这个商品的商家
     * 为空表示不筛选
     */
    searchText?: string | null;
}