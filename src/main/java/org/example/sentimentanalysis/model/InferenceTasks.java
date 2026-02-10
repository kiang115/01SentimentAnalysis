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
 * 推理任务详情表
 * </p>
 *
 * @author kiang
 * @since 2026-02-10
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("inference_tasks")
public class InferenceTasks implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推理任务 id (主键)
     */
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    /**
     * 发起人 id
     */
    @TableField("initiator_id")
    private Long initiatorId;

    /**
     * 推理开始时间
     */
    @TableField("inference_start_time")
    private LocalDateTime inferenceStartTime;

    /**
     * 推理结束时间
     */
    @TableField("inference_end_time")
    private LocalDateTime inferenceEndTime;

    /**
     * 推理持续时间 (单位: 毫秒)
     */
    @TableField("inference_duration")
    private Long inferenceDuration;

    /**
     * 评论数 (处理评论总数量)
     */
    @TableField("processed_count")
    private Integer processedCount;

    /**
     * 平均处理时间 (单位: 毫秒)
     */
    @TableField("avg_process_time")
    private BigDecimal avgProcessTime;

    /**
     * 使用模型 id json 存储，eg[1,2,3,4]
     */
    @TableField("used_model_ids")
    private String usedModelIds;

    /**
     * 涉及领域id json存储eg[0,3,4]
     */
    @TableField("domain_ids")
    private String domainIds;

    /**
     * 处理状态 (0:待处理, 1:处理中, 2:成功, 3:失败)
     */
    @TableField("process_status")
    private Byte processStatus;
}
