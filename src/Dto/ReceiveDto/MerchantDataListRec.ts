import type { PageInfo } from '@/Dto/ReceiveDto/PageInfo';

/**
 * 商家（Merchant）的具体结构
 */
export interface MerchantItem {
    merchantId: number;    // 商家编号
    name: string;          // 商家名称
    domainName: string;    // 领域名称
    description: string | null;   // 商家描述
    rating: number;        // 评分 (如 4.8)
    commentCount: number;  // 评论数量
    positiveRate: number;  // 好评率 (如 0.98)
    avatarUrl: string | null; // 头像地址，可能为 null
}

/**
 * 领域/分类的具体结构 (与之前一致)
 */
export interface DomainItem {
    domainId: number;      // 领域编号
    domainName: string;    // 领域名称
}

/**
 * Data 字段的具体结构
 */
export interface MerchantDataListRec {
    pageInfo: PageInfo<MerchantItem>; // 分页信息 + 商家列表
    domains: DomainItem[];            // 领域选择下拉框数据
}