package org.example.sentimentanalysis.dto.redisDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

//要从redis中拿到的数据
@Data
public class InferTaskSnapshot {
    private Long taskId;
    private Integer processedCount;
    private Double duration;

    // 关键点：匹配你的时间格式 "2026-02-13 20:17:02"
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime currentTime;

    private Integer status;
    private String statusMsg;
}
