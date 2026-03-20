/**
 * 添加评论的请求参数 (对应 Java 的 CommentAddRec)
 * 该 DTO 用于消费者在系统中针对特定商户的服务或产品发布文本评论 [2]
 */
export interface CommentAddSend {
    /**
     * 关联顾客表 ID (消费者唯一标识) [3]
     */
    customerId?: number | null;

    /**
     * 评论内容 (不能为空) [3]
     */
    content: string;

    /**
     * 评论所属领域 ID: 0=外卖(waimai), 1=电商(shop), 2=酒店(hotel) [3]
     */
    domainId: number;

    /**
     * 关联商品表 ID [3]
     */
    productId: number;

    /**
     * 关联商家表 ID [3]
     */
    merchantId: number;
}