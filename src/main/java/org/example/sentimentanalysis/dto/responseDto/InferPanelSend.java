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
public class InferPanelSend {
// 返回给模型推理参数配置看板的数据
    private List<InferencePanelDomainsDataDto> inferenceConfigDataList;

    // --- 内部类开始 ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
//    每个领域的数据
    public static class InferencePanelDomainsDataDto {
//        领域id
        private Long domainId;
//        领域名称
        private String domainName;
//        领域未推理的评论数
        private Long uninferencedCommentNums;
//        领域专属模型信息
        private List<DomainsModelDataDto> domainModelsDataList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainsModelDataDto {
//        模型id ->模型唯一
        private Long modelId;
//        模型版本号码
        private BigDecimal modelVersion;
    }
}