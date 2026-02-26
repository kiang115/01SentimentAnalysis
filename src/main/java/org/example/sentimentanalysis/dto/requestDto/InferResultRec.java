package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.constraints.NotNull;
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
public class InferResultRec {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "处理状态不能为空")
    private Integer processStatus;//#0待处理 1处理中 2完成 3失败

    @NotNull(message = "处理评论数不能为空")
    private Long processCount;

//    @NotNull(message = "任务结束时间为空")
    private String taskEndTime;

//    @NotNull(message = "处理进度不能为空")
    private Long taskDuration;

    // 可以是空集合 []，但不能是 null。同时开启级联校验以校验 List 内部的对象
//    @NotEmpty(message = "评论结果列表不能为null")
//    @Valid
    private List<CommentResultList> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentResultList {

//        @NotNull(message = "评论ID不能为空")
        private Long commentId;

//        @NotNull(message = "模型情感结果不能为空")
//        @Min(value = 0, message = "情感结果只能为0或1")
//        @Max(value = 1, message = "情感结果只能为0或1")
        private Integer modelSentiment; // 0 或 1

//        @NotNull(message = "积极概率不能为空")
        private BigDecimal positiveProb;

//        @NotNull(message = "消极概率不能为空")
        private BigDecimal negativeProb;

//        @NotNull(message = "置信度不能为空")
        private BigDecimal confidence;

//        @NotNull(message = "评论模型id不能为空")
        private Long modelId;

//        @NotNull(message = "评论领域id不能为空")
        private Long domainId;
    }
}
