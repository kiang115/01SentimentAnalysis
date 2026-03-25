/**
 * 添加商品的请求参数 (对应 Java 的 ProductAddRec)
 * 该 DTO 用于商户角色在系统中进行商品的增删改查管理 [2]
 */
export interface ProductAddSend {
    /**
     * 产品名 (不能为 null)
     */
    productName: string;

    /**
     * 产品详情 (不能为 null)
     */
    productDetail: string; 

    /**
     * 图片url (不能为 null)
     */
    imageUrl: string; 

    /**
     * 价格  大于0的小数，最多二位小数 非null
     */
    price: number;
}
