/**
 * 管理员评论历史列表查询参数
 * 对应后端 AllCommentDataListQueryRec
 */
export interface AllCommentDataListQuerySend {
    /**
     * 页码。
     * 允许值：正整数；前端首次查询通常传 1。
     */
    pageNum: number;

    /**
     * 每页数量。
     * 允许值：正整数或 null；传 null 时由后端按默认值 8 处理。
     */
    pageSize: number | null;

    /**
     * 发布时间排序方向。
     * 允许值：'asc' 表示按 publishTime 升序，'desc' 表示按 publishTime 降序，'' 表示走后端默认降序。
     */
    timeOrder: '' | 'asc' | 'desc';

    /**
     * 评论状态筛选值。
     * 允许值：0=待推理，1=推理中，2=已推理，3=审核中，4=已修正，5=已拒绝；
     * 不筛选时传 null。
     */
    statusId: 0 | 1 | 2 | 3 | 4 | 5 | null;

    /**
     * 领域ID筛选值。
     * 允许值：真实存在的领域ID；不筛选时传 null。
     */
    domainId: number | null;

    /**
     * 评论内容模糊搜索关键词。
     * 允许值：任意字符串；空字符串表示不按内容筛选。
     */
    content: string;

    /**
     * 最终情感筛选值。
     * 允许值：0=差评，1=好评；不筛选时传 null。
     */
    label: 0 | 1 | null;
}
