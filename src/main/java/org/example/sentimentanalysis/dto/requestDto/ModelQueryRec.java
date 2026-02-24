package org.example.sentimentanalysis.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelQueryRec {
    @Builder.Default
    private Integer pageNum = 1;
    @Builder.Default
    private Integer pageSize = 8;
    private Long modelId;
    private Long domainId;
    private String source;
}
