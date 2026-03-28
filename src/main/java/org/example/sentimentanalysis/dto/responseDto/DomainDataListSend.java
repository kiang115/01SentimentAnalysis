package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainDataListSend {
    private List<String> sourceNameList;
    private List<DomainDataInfo> domainDataList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainDataInfo {
        private Long domainId;
        private String domainName;
        private String domainUrl;
        private LocalDateTime createdAt;
        private String domainImageUrl;
        private String domainDescription;
        private List<Long> trainDataCountList;
        private Long commentTotal;
        private Long modelTotal;
        private Long merchantTotal;
    }
}
