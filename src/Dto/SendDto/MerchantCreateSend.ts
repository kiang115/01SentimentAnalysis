export interface MerchantCreateRec {
    // 下面的都必须校验非空
    name: string;
    domainId: number;
    description: string;
    avatarUrl: string;
}