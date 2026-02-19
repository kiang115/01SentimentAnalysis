package org.example.sentimentanalysis.dto.requestDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class TrainResultRec {

    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /**
     * 领域ID
     */
    private Long domainId;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 处理状态
     * 0-进行中, 1-完成, 2-异常
     */
    private Integer processStatus;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 训练时长（单位：秒）
     */
    private Double duration;

    /**
     * 训练数据的ID列表
     */
    private List<Long> results;

    /**
     * 准确率
     */
    private BigDecimal accuracy;

    /**
     * 精确率
     */
    private BigDecimal precisionRate;

    /**
     * 召回率
     */
    private BigDecimal recallRate;

    /**
     * F1分数
     */
    private BigDecimal f1Score;

    /**
     * 训练损失列表
     */
    private List<BigDecimal> trainLossList;

    /**
     * 训练准确率列表
     */
    private List<BigDecimal> trainAccList;

    /**
     * 验证损失列表
     */
    private List<BigDecimal> valLossList;

    /**
     * 验证准确率列表
     */
    private List<BigDecimal> valAccList;

    /**
     * 总批次数量
     */
    private Integer epochTotalBatches;
}
