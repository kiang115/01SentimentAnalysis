package org.example.sentimentanalysis.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 模型表
 * </p>
 *
 * @author kiang
 * @since 2026-02-26
 */
@Getter
@Setter
@ToString
@TableName("models")
@Accessors(chain = true)
public class Models implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    @TableId(value = "model_id", type = IdType.AUTO)
    private Long modelId;

    /**
     * 领域id
     */
    @TableField("domain_id")
    private Long domainId;

    /**
     * 模型版本号
     */
    @TableField("model_version")
    private String modelVersion;

    /**
     * 来源
     */
    @TableField("source")
    private String source;

    /**
     * 总推理数
     */
    @TableField("inferred_num")
    private Long inferredNum;

    /**
     * 被修正数(错误数)
     */
    @TableField("corrected_num")
    private Long correctedNum;

    /**
     * 最终准确率%
     */
    @TableField(value = "accuracy", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private BigDecimal accuracy;

    @TableField("train_task_id")
    private Long trainTaskId;

    @TableField("description")
    private String description;

    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 1表示删除，0表示没被删除
     */
    @TableField("deleted")
    private Integer deleted;

    @TableField("base_model_id")
    private Long baseModelId;
}
