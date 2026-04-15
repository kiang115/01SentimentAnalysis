package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationHistoryQueryRec {

    @NotNull(message = "目标ID不能为空")
    private Long targetId;  // 商铺ID 或 商品ID

    @NotNull(message = "类型不能为空")
    private Integer type;   // 0=商铺, 1=商品
}
