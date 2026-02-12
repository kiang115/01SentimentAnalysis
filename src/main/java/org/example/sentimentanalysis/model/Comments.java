package org.example.sentimentanalysis.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商家情感分析-评论主表
 * </p>
 *
 * @author kiang
 * @since 2026-02-12
 */
@Getter
@Setter
@ToString
@TableName("comments")
@Accessors(chain = true)
public class Comments implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论唯一标识
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    /**
     * 关联顾客表
     */
    @TableField("customer_id")
    private Long customerId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 评论所属领域：0=外卖(waimai)，1=电商(shop)，2=酒店(hotel)
     */
    @TableField("domain_id")
    private Long domainId;

    /**
     * 评论发布时间
     */
    @TableField("publish_time")
    private LocalDateTime publishTime;

    /**
     * 关联商品表
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 关联商家表
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 评论当前状态：0=待推理(pending)，1=推理中(inferring)，2=已推理(inferred)，3=审核中(reviewing)，4=已修正(corrected)，5=已拒绝(rejected)
     */
    @TableField("status")
    private Integer status;

    /**
     * 最终情感结果：1=好评(positive)，0=差评(negative)
     */
    @TableField("final_sentiment")
    private Integer finalSentiment;

    /**
     * 是否被重训过
     */
    @TableField("is_retrained")
    private Boolean isRetrained;
}
