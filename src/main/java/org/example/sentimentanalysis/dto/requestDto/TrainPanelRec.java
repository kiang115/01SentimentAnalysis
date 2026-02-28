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

//    为true 全新训练 false 增量训练
    @NotNull(message = "是否覆盖训练标识不能为空")
    private Boolean isOverTrain;
//    当overtrain为false时->全新模式，需要选择基础模型
    private Long  baseModelId;
}
