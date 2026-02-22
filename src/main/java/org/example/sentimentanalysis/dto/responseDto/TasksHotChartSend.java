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
public class TasksHotChartSend {
    //    这是一个反应推理+训练次数的月度热力图
    private String year;//热力图，当前年份
    private String month;//热力图，当前月份
    private Integer maxTotalCount;//热力图，一天训练+推理任务次数的最大值，用于设置热力图颜色
    //    按天列表
    private List<DailyData> dailyDataList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyData {
        //        注意数据库中的时间字段是LocalDateTime，这里的date只是日期部分，不取时间。判断时也要根据日期判断
        private String date;//本月每天日期，格式为yyyy-MM-dd。如果当天没有训练，下面的count通通为0
        //        统计次数，只看有几个记录，不用关心其状态是成功还是失败之类的
        private Integer inferCount;//对应推理任务表中推理开始时间
        private Integer trainCount;//对应训练任务表中训练开始时间
        private Integer totalCount;//inferCount+trainCount
    }
}
