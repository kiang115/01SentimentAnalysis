package org.example.sentimentanalysis.dto.redisDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainTaskSnapshot {
    //     任务id
    private Long taskId;
    //    已持续时间
    private Integer duration;
    //  处理状态
    private Integer status;
    private String statusMsg;
    private Integer currentEpoch;
    //   当前时间
    // 关键点：匹配你的时间格式 "2026-02-13 20:17:02"
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime currentTime;
    //    平均处理速度
    private Integer totalEpochs;
    private Integer currentBatch;
    private Integer epochTotalBatches;
    private Double progressPercent;
    private String processSpeed;
//    性能指标
    private Double accuracy;
    private Double precisionRate;
    private Double recallRate;
    private Double f1Score;
}
