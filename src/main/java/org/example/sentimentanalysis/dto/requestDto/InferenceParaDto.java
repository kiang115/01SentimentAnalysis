package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// 点击推理按钮，后端收到的参数
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceParaDto {
    @Valid
    @NotEmpty(message = "推理参数列表不能为空")
    private List<InferenceDomainPara> inferenceDomainPara;

    @NotBlank(message = "排序方式不能为空")
    private String sort;

    // 内部类：接收推理所需的模型参数
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InferenceDomainPara {

        @NotNull(message = "领域id不能为空")
        private Long domainId;

        @NotNull(message = "模型id不能为空")
        private Long modelId;

        @NotNull(message = "推理数量不能为空")
        private Long inferenceReviewNums;
    }
}
