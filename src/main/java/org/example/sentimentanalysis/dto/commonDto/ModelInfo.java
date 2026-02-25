package org.example.sentimentanalysis.dto.commonDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfo {
    private String domainUrl;
    private String modelVersion;
}
