package org.example.sentimentanalysis.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationHistoryChangeSend {

    /** 是否存在历史快照；false 时其余字段均为 0，前端不展示趋势 */
    private Boolean hasHistory;

    /** 综合得分差值（当前值 - 历史快照值） */
    private BigDecimal ratingDiff;

    /** 好评率差值（当前值 - 历史快照值），原始小数如 0.0123 */
    private BigDecimal positiveRateDiff;

    /** 评论数差值（当前值 - 历史快照值） */
    private Long commentCountDiff;
}
