package org.example.sentimentanalysis.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// 点击推理按钮，后端收到的参数
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceParaDto {
    private List<InferenceDomainPara> inferenceDomainPara;
    private String sort;

    // 内部类：接收推理所需的模型参数
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InferenceDomainPara {
        // 领域id
        private Long domainId;
        private String domainName;
        // 模型id
        private Long modelId;
        private BigDecimal modelVersion;
        //待推理评论数量
        private Long inferenceReviewNums;
    }
}
