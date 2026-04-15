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
public class ReputationHistoryLineChartSend {
    private List<String> timeList;
    private List<BigDecimal> ratingList;
    private List<BigDecimal> positiveRateList;
    private List<Long> commentCountList;
    private List<Long> rankingList;
}
