package org.example.sentimentanalysis.dto.responseDto;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainParaListSend {
    private PageInfo<TrainParaInfo> pageInfo;
    private List<DomainsInfo> domains;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainParaInfo {
        private Long paraId;
        private Long domainId;
        private Integer loraR;
        private Integer loraAlpha;
        private Integer epochs;
        private Integer batchSize;
        private BigDecimal learningRate;
        private Integer randomSeed;
        private List<String> loraModules;
        private BigDecimal trainSplitRatio;
        //    领域名称
        private String domainName;
    }
}


