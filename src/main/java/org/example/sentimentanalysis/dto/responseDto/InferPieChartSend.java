package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferPieChartSend {
    private List<String> pieNameList; // [已推理+未推理]用于下拉框
    private List<String> domainNameList;//领域名字，用于饼状图的扇区名字
    private List<PieData> inferredPie;//已经完成推理的评论(comments.status=2/3/4) 领域名字->评论数量
    private List<PieData> unInferredPie;//未完成推理的评论(comments.status=0/1) 领域名字->评论数量

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PieData {
        private String domainName;//领域名字
        private Long commentNum;// 对应评论数量
    }
}
