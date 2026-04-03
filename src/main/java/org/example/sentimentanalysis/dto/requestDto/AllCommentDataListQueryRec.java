package org.example.sentimentanalysis.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllCommentDataListQueryRec {
    /**
     * 页码。
     * 允许值：正整数；为空或小于等于0时，service层按1处理。
     */
    @Builder.Default
    private Integer pageNum = 1;

    /**
     * 每页数量。
     * 允许值：正整数；为空或小于等于0时，service层按8处理。
     */
    @Builder.Default
    private Integer pageSize = 8;

    /**
     * 发布时间排序方向。
     * 允许值：asc 表示按 publishTime 升序；
     * desc、空字符串或 null,其他值 均按 publishTime 降序处理。
     */
    private String timeOrder;

    /**
     * 评论状态筛选值，对应 CommentStatusEnum 的状态码。
     * 允许值：0=待推理，1=推理中，2=已推理，3=审核中，4=已修正，5=已拒绝；
     * null 或不在枚举内的值表示不筛选。
     */
    private Integer statusId;

    /**
     * 领域ID筛选值。
     * 允许值：数据库中真实存在的 domainId；
     * null 表示不筛选；
     * 非空但不存在时抛业务异常。
     */
    private Long domainId;

    /**
     * 评论内容模糊搜索关键词。
     * 允许值：任意非空白字符串；
     * null、空字符串或全空格时不生效。
     */
    private String content;

    /**
     * 最终情感筛选值，对应 comments.final_sentiment。
     * 允许值：0=差评，1=好评；
     * null 或其他值表示不筛选。
     */
    private Integer label;
}
