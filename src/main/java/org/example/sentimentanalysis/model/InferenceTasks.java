package org.example.sentimentanalysis.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 推理任务详情表
 * </p>
 *
 * @author kiang
 * @since 2026-02-11
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName(value = "inference_tasks", autoResultMap = true)
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
     * 平均处理速度 (单位: 条/秒)
     */
    @TableField("avg_process_speed")
    private BigDecimal avgProcessSpeed;

    /**
     * 使用模型 id json 存储，eg[1,2,3,4]
     */
    @TableField(value = "used_model_ids", typeHandler = Fastjson2TypeHandler.class)
    private List<Long> usedModelIds;

    /**
     * 涉及领域id json存储eg[0,3,4]
     */
    @TableField(value = "domain_ids", typeHandler = Fastjson2TypeHandler.class)
    private List<Long> domainIds;

    /**
     * 处理状态 ( 0:处理中, 1:成功完成, 2:失败完成)
     */
    @TableField("process_status")
    private Integer processStatus;
}
