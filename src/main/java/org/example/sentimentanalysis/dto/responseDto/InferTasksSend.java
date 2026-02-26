package org.example.sentimentanalysis.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferTasksSend {
    private List<InferTasksDetailSend> inferenceTasksList;
    private Integer ifAllFinished;

    /**
     * 推理任务详情DTO
     * 对应数据库表 inference_tasks
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InferTasksDetailSend {

        /**
         * 详情id（对应数据库 task_id）
         */
        private Long taskId;

        /**
         * 推理开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime inferenceStartTime;

        /**
         * 推理结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime inferenceEndTime;

        /**
         * 推理持续时间 (单位: 秒)
         */
        private Long inferenceDuration;

        /**
         * 处理评论数 (处理评论总数量)
         */
        private Long processedCount;

        /**
         * 平均处理速度 (单位: 条/秒)
         */
        private BigDecimal avgProcessSpeed;

        /**
         * 模型信息列表
         * 对应数据库 used_model_ids (JSON格式)，转换为包含模型id、名称、版本号的对象列表
         */
        private List<ModelInfo> modelInfoList;

        /**
         * 处理状态 (String类型)
         * 对应数据库 process_status (数字)，转换为易读的字符串（如：待处理/处理中/成功/失败）
         */
        private Integer status;
        private String statusMsg;

        /**
         * 模型信息内部类
         * 包含模型id、模型名称、模型版本号
         */
        @Data
        public static class ModelInfo {
            /**
             * 模型id
             */
            private Long modelId;
            /**
             * 模型版本号
             */
            private String modelVersion;
            //        模型领域
            private String domainName;
        }
    }
}
