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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 推理记录表
 * </p>
 *
 * @author kiang
 * @since 2026-02-12
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("inference_records")
public class InferenceRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推理记录ID
     */
    @TableId(value = "inference_id", type = IdType.AUTO)
    private Long inferenceId;

    /**
     * 推理任务ID
     */
    @TableField("inference_task_id")
    private Long inferenceTaskId;

    /**
     * 关联评论表
     */
    @TableField("comment_id")
    private Long commentId;

    /**
     * 关联模型表
     */
    @TableField("model_id")
    private Long modelId;

    /**
     * 评论所属领域
     */
    @TableField("domain_id")
    private Long domainId;

    /**
     * 模型推理的正面概率
     */
    @TableField("positive_prob")
    private BigDecimal positiveProb;

    /**
     * 模型推理的负面概率
     */
    @TableField("negative_prob")
    private BigDecimal negativeProb;

    /**
     * 模型判断结果
     */
    @TableField("model_sentiment")
    private Integer modelSentiment;

    /**
     * 推理完成时间
     */
    @TableField("inference_time")
    private LocalDateTime inferenceTime;

    /**
     * 推理置信度
     */
    @TableField("confidence")
    private BigDecimal confidence;
}
