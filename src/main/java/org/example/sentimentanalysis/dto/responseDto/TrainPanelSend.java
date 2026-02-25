package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainPanelSend {
    /**
     * 训练参数配置列表
     * 对应图中「List<TrainPara>」，这是顶层DTO的核心属性
     */
    private List<TrainPanelDomainDTO> trainParaList;

    // ===================== 内部子DTO：TrainPara =====================

    /**
     * 训练参数配置DTO
     * 对应图中「TrainPara」，包含所有参数+领域数据列表
     */


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainPanelDomainDTO {
        /**
         * 领域id（严格按图中拼写，若图中是domianId可直接修改）
         */
        private Long domainId;

        /**
         * 领域名称
         */
        private String domainName;

        /**
         * 已修正的评论数量
         */
        private Long correctedNum;

        /**
         * 上传的参数数量
         */
        private Long uploadNum;

        //        原始网络数据集的参数数量
        private Long originalNum;

        //     该领域的默认参数配置
        private List<TrainParaDTO> defaultParaList;

//        选择模型版本
        private List<Integer> majorVersionList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainParaDTO {
        /**
         * 参数配置id
         */
        private Long paraId;

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
    }
}
