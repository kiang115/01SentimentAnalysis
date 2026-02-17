package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sentimentanalysis.model.TrainData;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 要返回给模型推理的数据
public class TrainDataSend {
    private Long taskId;
    // 领域
    private Long domainId;
    private String domainUrl;
    // 模型版本 #后台需要转为字符串 比如1.1转为1.10
    private BigDecimal modelVersion;
    private List<TrainSourceData> trainDataList;
    //  参数配置 直接从TrainPanelRec中复制
    /**
     * LORA_R
     */
    private Integer loraR;

    /**
     * lora_alpha
     */
    private Integer loraAlpha;

    /**
     * 训练轮数
     */
    private Integer epochs;

    /**
     * 训练批次大小
     */
    private Integer batchSize;

    /**
     * 学习率
     */
    private BigDecimal learningRate;

    /**
     * 随机种子
     */
    private Integer randomSeed;

    /**
     * Lora模块列表
     */
    private List<String> loraModules;
    /**
     * 训练集比例
     */
    private BigDecimal trainSplitRatio;
    // 是否需要覆盖训练
    private boolean isOverTrain;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainSourceData {
//        TrainData中得到
        //        类别名称 固定为 corrected/upload/original
        private String source;
        //        类别数据列表
//        从trainData对应的来源+领域 中随机筛选，不足就全部用上
        private List<TrainBaseData> trainDataList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainBaseData {
//        数据来源
        private Long id;
        private String content;
    }
}