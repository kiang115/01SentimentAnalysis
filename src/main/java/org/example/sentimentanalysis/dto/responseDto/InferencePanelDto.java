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
public class InferencePanelDto {

    private List<InferencePanelDomainsDataDto> inferenceConfigDataList;

    // --- 内部类开始 ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InferencePanelDomainsDataDto { // 加上 public static
        private Long domainId;
        private String domainName;
        private Long uninferencedCommentNums;
        private List<DomainsModelDataDto> domainModelsDataList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainsModelDataDto { // 加上 public static
        private Long modelId;
        private BigDecimal modelVersion;
    }
}