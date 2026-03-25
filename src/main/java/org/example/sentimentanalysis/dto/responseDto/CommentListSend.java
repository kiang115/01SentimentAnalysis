package org.example.sentimentanalysis.dto.responseDto;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentListSend {
    private PageInfo<CommentInfo> pageInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentInfo {
        private Long commentId;
        private Long userId;
        private String userName;
        private String userAvatarUrl;
        private String content;
        private LocalDateTime publishTime;
        private Integer statusId;
        private String statusName;
        private Integer finalSentimentCode;
        private String finalSentimentName;
        private BigDecimal positiveProb;
        private BigDecimal negativeProb;
        private BigDecimal confidence;
        private Boolean isInspected;
    }
}
