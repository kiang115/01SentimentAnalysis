package org.example.sentimentanalysis.dto.responseDto;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllCommentDataListSend {
    /**
     * 评论历史分页结果。
     */
    private PageInfo<AllCommentDataInfo> pageInfo;

    /**
     * 全量领域信息列表，用于前端筛选器展示。
     */
    private List<DomainsInfo> domains;

    /**
     * 全量评论状态列表，用于前端筛选器展示。
     */
    private List<CommentStatusInfo> commentStatusList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentStatusInfo {
        /**
         * 评论状态码。
         * 允许值：0=待推理，1=推理中，2=已推理，3=审核中，4=已修正，5=已拒绝。
         */
        private Integer statusCode;

        /**
         * 评论状态中文名。
         * 允许值：待推理、推理中、已推理、审核中、已修正、已拒绝。
         */
        private String statusName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllCommentDataInfo {
        /**
         * 评论主键ID。
         * 允许值：comments.comment_id，正整数。
         */
        private Long commentId;

        /**
         * 评论内容原文。
         * 允许值：comments.content，非空字符串；若数据库异常缺失则可能为 null。
         */
        private String content;

        /**
         * 最新推理记录中的模型情感结果码。
         * 允许值：0=差评，1=好评；
         * 该评论没有推理记录时为 null。
         */
        private Integer inferResultCode;

        /**
         * 最新推理记录中的模型情感结果中文名。
         * 允许值：差评、好评；
         * 该评论没有推理记录时为 null。
         */
        private String inferResultName;

        /**
         * 最新推理记录中的正向概率。
         * 允许值：0到1之间的小数；
         * 该评论没有推理记录时为 null。
         */
        private BigDecimal positiveProb;

        /**
         * 最新推理记录中的负向概率。
         * 允许值：0到1之间的小数；
         * 该评论没有推理记录时为 null。
         */
        private BigDecimal negativeProb;

        /**
         * 评论所属领域ID。
         * 允许值：comments.domain_id，正整数；关联数据缺失时仍返回评论表中的原值。
         */
        private Long domainId;

        /**
         * 评论所属领域名称。
         * 允许值：domains.domain_name；
         * 关联领域记录缺失时为 null。
         */
        private String domainName;

        /**
         * 评论所属商品ID。
         * 允许值：comments.product_id，正整数；关联数据缺失时仍返回评论表中的原值。
         */
        private Long productId;

        /**
         * 评论所属商品名称。
         * 允许值：products.name；
         * 关联商品记录缺失时为 null。
         */
        private String productName;

        /**
         * 商品头像URL。
         * 允许值：products.image_url；
         * 未设置或关联商品缺失时为 null。
         */
        private String productImageUrl;

        /**
         * 评论所属商家ID。
         * 允许值：comments.merchant_id，正整数；关联数据缺失时仍返回评论表中的原值。
         */
        private Long merchantId;

        /**
         * 评论所属商家名称。
         * 允许值：merchants.name；
         * 关联商家记录缺失时为 null。
         */
        private String merchantName;

        /**
         * 商家头像URL。
         * 允许值：merchants.avatar_url；
         * 未设置或关联商家缺失时为 null。
         */
        private String merchantAvatarUrl;

        /**
         * 评论用户名称。
         * 允许值：users.user_name；
         * 关联用户记录缺失时为 null。
         */
        private String customerName;

        /**
         * 评论用户头像URL。
         * 允许值：users.avatar_url；
         * 未设置或关联用户缺失时为 null。
         */
        private String customerAvatarUrl;

        /**
         * 评论当前状态码。
         * 允许值：0=待推理，1=推理中，2=已推理，3=审核中，4=已修正，5=已拒绝；
         * 若数据库出现异常值，则原样返回。
         */
        private Integer statusId;

        /**
         * 评论当前状态中文名。
         * 允许值：待推理、推理中、已推理、审核中、已修正、已拒绝；
         * 若状态码为空或异常，返回 null。
         */
        private String statusName;

        /**
         * 评论发布时间。
         * 允许值：comments.publish_time；
         * 正常情况下为非空时间。
         */
        private LocalDateTime publishTime;

        /**
         * 评论最终情感结果码，对应 comments.final_sentiment。
         * 允许值：0=差评，1=好评；
         * 未推理或未写入结果时为 null。
         */
        private Integer finalSentimentCode;

        /**
         * 评论最终情感结果中文名。
         * 允许值：差评、好评；
         * finalSentimentCode 为 null 或非法时为 null。
         */
        private String finalSentimentName;

        /**
         * 是否已做标签分析。
         * 允许值：true=已分析，false=未分析；
         * 数据异常时可能为 null。
         */
        private Boolean isInspected;

        /**
         * 最新推理记录所用模型的版本号。
         * 允许值：models.model_version；
         * 该评论没有推理记录或模型记录缺失时为 null。
         */
        private String modelVersion;
    }
}
