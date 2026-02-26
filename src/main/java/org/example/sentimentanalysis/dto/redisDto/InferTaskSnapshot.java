package org.example.sentimentanalysis.dto.redisDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

//要从redis中拿到的数据
@Data
public class InferTaskSnapshot {
//     任务id
    private Long taskId;
//    已处理评论数
    private Integer processedCount;
//    已持续时间
    private Integer duration;
//   当前时间
    // 关键点：匹配你的时间格式 "2026-02-13 20:17:02"
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime currentTime;
//  处理状态
//    (0, "处理中"),
//     (1, "正常完成"),
//     (2, "异常"),
//     (-1, "未知状态");
    private Integer status;
// 模型服务端传入的状态描述 三种情况
//    processing completed  Error:+ (自定义的错误信息)
    private String statusMsg;
//    平均处理速度
    private Double processSpeed;
}
