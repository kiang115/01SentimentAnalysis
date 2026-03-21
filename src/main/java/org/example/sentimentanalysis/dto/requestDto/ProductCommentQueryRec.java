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
public class ProductCommentQueryRec {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    private Integer pageNum;
    private Integer pageSize;
}
