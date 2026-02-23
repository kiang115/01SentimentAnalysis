package org.example.sentimentanalysis.dto.requestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainDataAddRec {
    @NotNull(message = "内容不能为空")
    private String content;
    @NotNull(message = "标签不能为空")
    private Integer label;
    @NotNull(message = "领域ID不能为空")
    private Long domainId;
}
