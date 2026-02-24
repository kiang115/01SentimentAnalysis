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
public class ModelAddRec {
    @NotNull(message = "domainId不能为空")
    private Long domainId;

    @NotBlank(message = "modelVersion不能为空")
    private String modelVersion;

    private String description;
}
