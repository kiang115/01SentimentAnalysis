/**
 * 商品标签分析统计结果 (对应 Java 的 ProductTagAnalyzeSend)
 * 用于反馈商品评论处理后的标签变动及分析进度情况
 */
export interface ProductTagAnalyzeRec {
    /**
     * 商品ID
     */
    productId: number;

    /**
     * 已处理的评论总数
     * 表示本次分析任务中成功解析的评论条数
     */
    processedCommentCount: number;

    /**
     * 涉及/触发的现有标签数量
     * 表示在分析过程中，命中了多少个该商品已有的标签
     */
    touchedTagCount: number;

    /**
     * 新发现/生成的标签数量
     * 表示通过模型推理，从评论中新提取出的标签个数
     */
    newTagCount: number;
}