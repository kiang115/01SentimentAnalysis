package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainPanelRec {

    @NotNull(message = "领域ID不能为空")
    private Long domainId;

    @NotNull(message = "修正数据数量不能为空")
    private Long correctedNum;

    @NotNull(message = "上传数据数量不能为空")
    private Long uploadNum;

    @NotNull(message = "原始参数数量不能为空")
    private Long originalNum;

    @NotNull(message = "LoRA秩(loraR)不能为空")
    private Integer loraR;

    @NotNull(message = "LoRA Alpha不能为空")
    private Integer loraAlpha;

    @NotNull(message = "训练轮数不能为空")
    private Integer epochs;

    @NotNull(message = "批次大小不能为空")
    private Integer batchSize;

    @NotNull(message = "学习率不能为空")
    private BigDecimal learningRate;

    @NotNull(message = "随机种子不能为空")
    private Integer randomSeed;

    @NotEmpty(message = "LoRA模块列表不能为空且至少选择一个")
    private List<String> loraModules;

    @NotNull(message = "训练集比例不能为空")
    private BigDecimal trainSplitRatio;

    /**
     * 建议使用 Boolean 包装类型，以便进行 @NotNull 验证
     * 如果使用基本类型 boolean，它默认为 false，无法判断是用户传的还是默认的
     */
    @NotNull(message = "是否覆盖训练标识不能为空")
    private Boolean isOverTrain;
//    如果不是重训模式，需要选择大模型版本
    private Integer  majorVersion;
}
