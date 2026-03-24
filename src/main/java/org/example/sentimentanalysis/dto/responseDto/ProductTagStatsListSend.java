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
public class ProductTagStatsListSend {
    private List<ProductTagStats> productTagStatsList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductTagStats {
        private Long tagId;
        private String tagName;
        private Integer positiveCount;
        private Integer negativeCount;
        private Integer totalCount;
        private LocalDateTime updateTime;
    }
}


