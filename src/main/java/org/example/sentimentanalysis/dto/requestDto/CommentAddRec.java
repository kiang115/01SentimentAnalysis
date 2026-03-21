package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentAddRec {

    private Long customerId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    @NotNull(message = "领域ID不能为空")
    private Long domainId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "商铺ID不能为空")
    private Long merchantId;
}
