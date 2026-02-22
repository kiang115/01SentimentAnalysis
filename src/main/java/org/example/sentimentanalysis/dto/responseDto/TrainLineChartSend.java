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
public class TrainLineChartSend {
    private List<String> domainNameList;
    private List<DomainLinesData> domainLinesDataList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainLinesData {
        private String domainName;
        private List<String> allSmallVersions; //折线图的横坐标 即所有大版本的小版本号集合 X.0 X.1...X.10 X.11...
        private List<String> allMajorVersions; //多条折线区分 大版本号
        private List<MajorVersionData> majorVersionDataList; //多条折线的数据
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorVersionData {
        private String majorVersion; //大版本号名字
        private List<BigDecimal> versionAccuracyList; //对应的小版本号的准确率 没有的用null表示
    }
}
