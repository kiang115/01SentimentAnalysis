import type { PageInfo } from "./PageInfo";

/**
 * 评论列表返回结构 (对应 Java 的 CommentListSend)
 */
export interface CommentDataListRec {
    /**
     * 分页信息，包含评论详情列表
     */
    pageInfo: PageInfo<CommentInfo>;
}

/**
 * 单条评论详细信息 (对应 Java 的 CommentInfo)
 * 整合了评论基本信息 [3]、用户信息以及模型推理结果 [3]
 */
export interface CommentInfo {
    /**
     * 评论唯一标识 (comment_id) 
     */
    commentId: number;

    /**
     * 用户 ID (关联顾客表 customer_id) 
     */
    userId: number;

    /**
     * 用户名称
     */
    userName: string;

    /**
     * 用户头像 URL (可能为 null)
     */
    userAvatarUrl: string | null;

    /**
     * 评论内容 
     */
    content: string;

    /**
     * 评论发布时间 (ISO 格式字符串) 
     */
    publishTime: string;

    /**
     * 状态码: 0=待推理, 1=推理中, 2=已推理, 3=审核中, 4=已修正, 5=已拒绝 
     */
    statusId: number;

    /**
     * 状态描述名称 (如: "已推理")
     */
    statusName: string;

    /**
     * 最终情感代码: 1=好评(positive), 0=差评(negative) 
     */
    finalSentimentCode: number;

    /**
     * 最终情感名称 (如: "好评")
     */
    finalSentimentName: string;

    /**
     * 模型推理的正面概率
     */
    positiveProb: number;

    /**
     * 模型推理的负面概率
     */
    negativeProb: number;

    /**
     * 推理置信度
     */
    confidence: number;

    /**
     * 标签分析状态: true=已分析, false/null=待分析
     */
    isInspected: boolean | null;
}
