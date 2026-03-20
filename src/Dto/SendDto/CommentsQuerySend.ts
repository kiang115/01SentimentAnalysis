/**
 * 商品评论查询请求参数 (对应 Java 的 ProductCommentQueryRec)
 * 用于分页检索特定商品的评论列表，支撑消费者决策或商户口碑查看 [3]
 */
export interface CommentsQuerySend {
    /**
     * 商品ID (必填，关联评论主表的 product_id) [1]
     */
    productId: number;

    /**
     * 页码 (可选)
     */
    pageNum?: number;

    /**
     * 每页数量 (可选)
     */
    pageSize?: number;
}