package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelPieChartSend {
    //    统计不同状态评论的数量
    private List<String> statusNameList;
    private List<Long> statusCountList;
    //    统计不同领域的整体准确度
    private List<String> domainNameList;
    private List<BigDecimal> domainAccuracyList;
    private List<Long> inferredNumList;
    private List<Long> rightNumList;
}

