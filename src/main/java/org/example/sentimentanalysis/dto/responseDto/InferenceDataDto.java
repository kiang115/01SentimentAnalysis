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
public class InferenceDataDto {
    private List<InferenceDomainData> inferenceDomainDataList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InferenceDomainData {
        // 领域
        private Long domainId;
        private String domainName;
        // 模型id
        private Long modelId;
        private BigDecimal modelVersion;
        // 本领域待推理评论数
        private Long inferenceCommentNums;
        // 需要推理的评论列表
        private List<InferenceDomainComment> inferenceDomainCommentList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InferenceDomainComment {
        //评论id
        private Long commentId;
        //评论内容
        private String content;
    }
}
