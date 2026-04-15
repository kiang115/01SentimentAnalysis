import type { PageInfo } from "@/Dto/ReceiveDto/PageInfo";

/**
 * 管理员评论历史列表返回结构
 * 对应后端 AllCommentDataListSend
 */
export interface AllCommentDataListRec {
    /**
     * 分页信息，包含评论历史列表。
     */
    pageInfo: PageInfo<AllCommentDataItem>;

    /**
     * 全量领域列表，用于筛选下拉框。
     */
    domains: DomainItem[];

    /**
     * 全量评论状态列表，用于筛选下拉框。
     */
    commentStatusList: CommentStatusItem[];
}

/**
 * 单条评论历史数据
 * 对应后端 AllCommentDataInfo
 */
export interface AllCommentDataItem {
    /**
     * 评论主键ID。
     */
    commentId: number;

    /**
     * 评论内容原文。
     */
    content: string | null;

    /**
     * 最新推理结果代码。
     * 允许值：0=差评，1=好评；没有推理记录时为 null。
     */
    inferResultCode: 0 | 1 | null;

    /**
     * 最新推理结果中文名。
     * 允许值：差评、好评；没有推理记录时为 null。
     */
    inferResultName: string | null;

    /**
     * 最新推理记录中的正向概率。
     * 没有推理记录时为 null。
     */
    positiveProb: number | null;

    /**
     * 最新推理记录中的负向概率。
     * 没有推理记录时为 null。
     */
    negativeProb: number | null;

    /**
     * 评论所属领域ID。
     */
    domainId: number | null;

    /**
     * 评论所属领域名称。
     * 关联领域缺失时为 null。
     */
    domainName: string | null;

    /**
     * 评论所属商品ID。
     */
    productId: number | null;

    /**
     * 评论所属商品名称。
     * 关联商品缺失时为 null。
     */
    productName: string | null;

    /**
     * 商品头像地址。
     * 未设置或关联商品缺失时为 null。
     */
    productImageUrl: string | null;

    /**
     * 评论所属商家ID。
     */
    merchantId: number | null;

    /**
     * 评论所属商家名称。
     * 关联商家缺失时为 null。
     */
    merchantName: string | null;

    /**
     * 商家头像地址。
     * 未设置或关联商家缺失时为 null。
     */
    merchantAvatarUrl: string | null;

    /**
     * 评论用户名称。
     * 关联用户缺失时为 null。
     */
    customerName: string | null;

    /**
     * 评论用户头像地址。
     * 未设置或关联用户缺失时为 null。
     */
    customerAvatarUrl: string | null;

    /**
     * 评论当前状态码。
     * 允许值：0=待推理，1=推理中，2=已推理，3=审核中，4=已修正，5=已拒绝。
     */
    statusId: 0 | 1 | 2 | 3 | 4 | 5 | null;

    /**
     * 评论当前状态中文名。
     */
    statusName: string | null;

    /**
     * 评论发布时间，ISO 字符串格式。
     */
    publishTime: string | null;

    /**
     * 评论最终情感结果码。
     * 允许值：0=差评，1=好评；未写入时为 null。
     */
    finalSentimentCode: 0 | 1 | null;

    /**
     * 评论最终情感结果中文名。
     */
    finalSentimentName: string | null;

    /**
     * 是否已做标签分析。
     * 允许值：true=已分析，false=未分析；异常情况下可能为 null。
     */
    isInspected: boolean | null;

    /**
     * 最新推理记录所用模型的版本号。
     * 没有推理记录或模型记录缺失时为 null。
     */
    modelVersion: string | null;
}

/**
 * 领域下拉项
 */
export interface DomainItem {
    /**
     * 领域ID。
     */
    domainId: number;

    /**
     * 领域名称。
     */
    domainName: string;

    /**
     * 领域图片地址。
     * 后端会返回该字段，未设置时可能为 null。
     */
    domainImageUrl: string | null;
}

/**
 * 评论状态下拉项
 */
export interface CommentStatusItem {
    /**
     * 状态码。
     * 允许值：0=待推理，1=推理中，2=已推理，3=审核中，4=已修正，5=已拒绝。
     */
    statusCode: 0 | 1 | 2 | 3 | 4 | 5;

    /**
     * 状态中文名。
     */
    statusName: string;
}
