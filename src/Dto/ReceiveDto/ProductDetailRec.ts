/**
 * 商品详情数据结构 (对应 Java 的 ProductDetailSend)
 */
export interface ProductDetailRec {
    /**
     * 商品id
     */
    productId: number;

    /**
     * 商品名字
     */
    name: string;

    /**
     * 商品图片url (可能为 null)
     */
    imageUrl: string | null;

    /**
     * 商品详情
     */
    details: string;

    /**
     * 商品价格 单位￥ (Java 中的 BigDecimal 对应 TS 的 number)
     */
    price: number;

    /**
     * 综合得分
     */
    rating: number;

    /**
     * 评论数
     */
    commentCount: number;

    /**
     * 商铺id
     */
    merchantId: number;

    /**
     * 商铺名字
     */
    merchantName: string;

    /**
     * 好评率
     */
    positiveRate: number;
}