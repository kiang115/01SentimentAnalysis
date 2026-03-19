/**
 * 商铺详情数据结构 (对应 Java 的 MerchantDetailSend)
 */
export interface MerchantDetailRec {
    /**
     * 商铺id
     */
    merchantId: number;

    /**
     * 商铺名称
     */
    name: string;

    /**
     * 所属领域id (0=外卖, 1=电商, 2=酒店)
     */
    domainId: number;

    /**
     * 所属领域名称
     */
    domainName: string;

    /**
     * 商铺详情描述
     */
    description: string;

    /**
     * 商铺综合评分
     */
    rating: number;

    /**
     * 商铺总评论人数
     */
    commentCount: number;

    /**
     * 商铺好评率
     */
    positiveRate: number;

    /**
     * 商铺头像/封面图url (可能为 null)
     */
    avatarUrl: string | null;

    /**
     * 该商铺下的商品列表
     */
    products: ProductBriefSend[];
}

/**
 * 商品简要信息结构 (对应 Java 的 ProductBriefSend)
 */
export interface ProductBriefSend {
    /**
     * 商品id
     */
    productId: number;

    /**
     * 商品名称
     */
    name: string;

    /**
     * 商品详情描述
     */
    details: string;

    /**
     * 商品综合评分
     */
    rating: number;

    /**
     * 商品评论人数
     */
    commentCount: number;

    /**
     * 商品价格
     */
    price: number;

    /**
     * 商品图片url (可能为 null)
     */
    imageUrl: string | null;
}