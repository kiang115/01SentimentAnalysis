/**
 * 商品标签统计列表返回结构 (对应 Java 的 ProductTagStatsListSend)
 */
export interface ProductTagStatsListRec {
    /**
     * 商品标签统计详情列表
     */
    productTagStatsList: ProductTagStats[];
}

/**
 * 单个标签的统计信息 (对应 Java 的 ProductTagStats)
 * 记录了特定标签下正向与负向评价的分布情况
 */
export interface ProductTagStats {
    /**
     * 标签唯一标识 ID
     */
    tagId: number;

    /**
     * 标签名称 (例如: "味道赞", "物流快")
     */
    tagName: string;

    /**
     * 该标签下的正向评价(好评)数量
     */
    positiveCount: number;

    /**
     * 该标签下的负向评价(差评)数量
     */
    negativeCount: number;

    /**
     * 该标签关联的评论总数
     */
    totalCount: number;

    /**
     * 统计数据最后更新时间 (ISO 格式字符串)
     */
    updateTime: string;
}