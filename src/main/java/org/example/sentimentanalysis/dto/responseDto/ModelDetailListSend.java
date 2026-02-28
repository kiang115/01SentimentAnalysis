package org.example.sentimentanalysis.dto.responseDto;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.sentimentanalysis.dto.commonDto.DomainsInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDetailListSend {
    private PageInfo<ModelDetailInfo> pageInfo;
    private List<DomainsInfo> domains;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
//    todo 筛选状态为ok的传入给后端即可
    public static class ModelDetailInfo {
        private Long modelId;
        private Long domainId;
        private String domainName;
        private String modelVersion;
        private String sourceName;
        private Long inferredNum;
        private Long correctedNum;
        private BigDecimal accuracy;
        private String description;
        private LocalDateTime createdAt;
        private String baseModelVersion;
    }
}
